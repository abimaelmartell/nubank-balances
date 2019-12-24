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
  (->>
    operations
    (sort utils/sort-by-date)
    (utils/group-operations)))

(defn operations->statement
  [operations]
  (loop [balance    0
         result     {}
         remaining (operations->groups operations)]
    (if (empty? remaining)
      result
      (let [[current-group & rest] remaining
            current-balance (+ balance (calculate-balance (second current-group)))
            current-date (first current-group)
            current-date-operations (second current-group)]
        (recur
          current-balance
          (assoc result current-date { :operations current-date-operations :balance current-balance })
          rest)))))

(defn operations->periods-of-debt
  [operations]
  (loop [balance 0
         result []
         remaining (operations->groups operations)]
    (if (empty? remaining)
      (reverse result)
      (let [[current-group & rest] remaining
            [last-debt & rest-debts] result
            current-balance (+ balance (calculate-balance (second current-group)))
            is-debt (neg? current-balance)
            is-balance-change (not= current-balance balance)
            is-principal-change (and is-balance-change is-debt)
            is-debt-end (and is-balance-change (and last-debt (nil? (last-debt :end))))
            next-result (if is-debt-end rest-debts result)]
        (recur
          current-balance
          (cond-> next-result
            is-debt-end
              (conj (assoc last-debt :end (utils/minus-one-day (first current-group))))
            is-principal-change
              (conj { :principal (math/abs current-balance) :start (first current-group) }))
          rest)))))

(defn filter-statement-by-date
  [statement starting ending]
    (filter #(t/within? starting ending (utils/parse-date (first %))) statement))
