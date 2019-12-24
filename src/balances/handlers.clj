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
  (let [{{ :keys [account-id starting ending] } :params } req]
    (as-> nil %
      (store/account-operations account-id)
      (logic/operations->statement %)
      ; if params `starting` and `ending` are provided
      ; it will filter the statement to only include
      ; those dates, otherwise will include all operations
      (if (validations/are-valid-statement-dates? starting ending)
        (logic/filter-statement-by-date
          %
          (parse-date starting)
          (parse-date ending))
        (identity %))
      (data->json %))))

(defn periods-of-debt-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->
      (store/account-operations account-id)
      (logic/operations->periods-of-debt)
      (data->json))))
