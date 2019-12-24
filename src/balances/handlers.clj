(ns balances.handlers
  (:require [balances.store :as store]
            [balances.logic :as logic]
            [balances.utils :refer :all]
            [balances.validations :as validations])
  (:use [clojure.string :only (join)]))

(defn operation-handler
  [req]
  (let [{ :keys [body params] } req
        { account-id :account-id } params
        operation-data (parse-json (slurp body))
        error (validations/validate-operation operation-data)]
    (if (nil? error)
      (do
        (store/save-operation! account-id (map->operation operation-data))
        (data->json {:success true }))
      (data->json
        {:success false
         :error error}))))

(defn balance-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->>
      (store/account-operations account-id)
      (logic/calculate-balance)
      (hash-map :balance)
      (data->json))))

(defn statement-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->
      (store/account-operations account-id)
      (logic/operations->statement)
      (data->json))))

(defn periods-of-debt-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->
      (store/account-operations account-id)
      (logic/operations->periods-of-debt)
      (data->json))))
