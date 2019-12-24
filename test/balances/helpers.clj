(ns balances.helpers
  (:require [balances.utils :refer :all]))

(defn debit-operation-json
  ([] (debit-operation-json "1/11/2019" 100.00))
  ([date] (debit-operation-json date 100.00))
  ([date amount] { :type "debit" :amount amount :date date }))

(defn credit-operation-json
  ([] (credit-operation-json "1/11/2019" 20.00 "McDonalds"))
  ([date] (credit-operation-json date 20.00 "McDonalds"))
  ([date amount] { :type "credit" :amount amount :date date, :merchant "McDonalds" })
  ([date amount merchant] { :type "credit" :amount amount :date date, :merchant merchant }))

(defn debit-operation
  [& args]
  (map->operation (apply debit-operation-json args)))

(defn credit-operation
  [& args]
  (map->operation (apply credit-operation-json args)))
