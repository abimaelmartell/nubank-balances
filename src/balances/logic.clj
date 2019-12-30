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
  (group-by #(% :date) operations))

(defn sort-operations-by-date
  [operations]
  (sort utils/sort-by-date operations))

(defn operations->statement
  [operations]
  (loop [balance 0
         result []
         remaining (operations->groups operations)]
    (if (empty? remaining)
      result
      (let [[[date operations] & rest] remaining
            current-balance (+ balance (calculate-balance operations))]
        (recur
          current-balance
          (conj result { :date date :operations operations :balance current-balance })
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
            last-debt-open (and last-debt (nil? (last-debt :end)))
            is-debt-end (and is-balance-change last-debt-open)]
        (recur
          current-balance
          (as-> [] <>
            (if is-debt-end
              (conj rest-debts (assoc last-debt :end (utils/minus-one-day date)))
              result)
            (if is-principal-change
              (conj <> { :principal (math/abs current-balance) :start date })
              <>))
          rest)))))

(defn filter-statement-by-date
  [statement starting ending]
  (filter #(t/within? starting ending (% :date)) statement))
