(ns balances.helpers
  (:require
    [clojure.data.json :as json]
    [balances.utils :refer [map->operation]]))

(defn debit-operation-json
  ([] (debit-operation-json "1/11/2019" 100))
  ([date] (debit-operation-json date 100))
  ([date amount] { :type "debit" :amount amount :date date }))

(defn credit-operation-json
  ([] (credit-operation-json "1/11/2019" 20 "McDonalds"))
  ([date] (credit-operation-json date 20 "McDonalds"))
  ([date amount] { :type "credit" :amount amount :date date, :merchant "McDonalds" })
  ([date amount merchant] { :type "credit" :amount amount :date date, :merchant merchant }))

(defn debit-operation
  [& args]
  (map->operation (apply debit-operation-json args)))

(defn credit-operation
  [& args]
  (map->operation (apply credit-operation-json args)))

(defn operation->json
  [data]
  (json/write-str data))

(defn parse-json
  [json]
  (json/read-str json :key-fn keyword))
