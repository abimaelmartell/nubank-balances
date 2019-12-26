(ns balances.logic
  (:require [balances.utils :as utils]
            [clojure.math.numeric-tower :as math]
            [clj-time.core :as t]))

(defn calculate-balance
  [operations]
  (reduce
    (fn [balance operation]
      (case (operation :type)
        :debit (+ balance (operation :amount))
        :credit (- balance (operation :amount))))
    0
    operations))

(defn operations->groups
  [operations]
  (group-by #(utils/unparse-date (% :date)) operations))

(defn sort-operations-by-date
  [operations]
  (sort utils/sort-by-date operations))

(defn operations->statement
  [operations]
  (loop [balance    0
         result     {}
         remaining (operations->groups operations)]
    (if (empty? remaining)
      result
      (let [[[date operations] & rest] remaining
            current-balance (+ balance (calculate-balance operations))]
        (recur
          current-balance
          (assoc result date { :operations operations :balance current-balance })
          rest)))))

(defn operations->periods-of-debt
  [operations]
  (loop [balance 0
         result []
         remaining (operations->groups operations)]
    (if (empty? remaining)
      (reverse result)
      (let [[[date operations] & rest] remaining
            [last-debt & rest-debts] result
            current-balance (+ balance (calculate-balance operations))
            is-debt (neg? current-balance)
            is-balance-change (not= current-balance balance)
            is-principal-change (and is-balance-change is-debt)
            is-debt-end (and is-balance-change (and last-debt (nil? (last-debt :end))))
            next-result (if is-debt-end rest-debts result)]
        (recur
          current-balance
          (cond-> next-result
            is-debt-end
              (conj (assoc last-debt :end (utils/minus-one-day date)))
            is-principal-change
              (conj { :principal (math/abs current-balance) :start date }))
          rest)))))

(defn filter-statement-by-date
  [statement starting ending]
  (into {}
        (filter #(t/within? starting ending (utils/parse-date (first %))) statement)))
