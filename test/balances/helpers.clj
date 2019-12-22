(ns balances.helpers
  (:require [balances.utils :refer :all]))

(defn debit-operation
  ([] (debit-operation "1/11/2019" 100.00))
  ([date] (debit-operation date 100.00))
  ([date amount] { :type :debit :amount amount :date (parse-date date) }))

(defn credit-operation
  ([] (credit-operation "1/11/2019" 20.00))
  ([date] (credit-operation date 20.00))
  ([date amount] { :type :credit :amount amount :date (parse-date date) }))

