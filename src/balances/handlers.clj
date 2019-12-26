(ns balances.handlers
  (:require [balances.store :as store]
            [balances.logic :as logic]
            [balances.utils :refer :all]
            [balances.validations :as validations])
  (:use [clojure.string :only (join)]))

(defn operation-handler
  [{ body :body { account-id :account-id } :params }]
  (let [operation-data (parse-json (slurp body))
        error (validations/validate-operation operation-data)]
    (if (nil? error)
      (do
        (store/save-operation! account-id (map->operation operation-data))
        (data->json {:success true }))
      (data->json
        {:success false
         :error error}))))

(defn balance-handler
  [{{ account-id :account-id } :params }]
  (->>
    (store/account-operations account-id)
    (logic/calculate-balance)
    (hash-map :balance)
    (data->json)))

(defn- maybe-filter-by-date
  [operations starting ending]
  (if (validations/are-valid-statement-dates? starting ending)
    (logic/filter-statement-by-date
      operations
      (parse-date starting)
      (parse-date ending))
    operations))

(defn statement-handler
  [{{ :keys [account-id starting ending] } :params }]
  (->
    (store/account-operations account-id)
    (logic/sort-operations-by-date)
    (logic/operations->statement)
    ; if params `starting` and `ending` are provided
    ; it will filter the statement to only include
    ; those dates, otherwise will include all operations
    (maybe-filter-by-date starting ending)
    (data->json)))

(defn periods-of-debt-handler
  [{{ account-id :account-id } :params }]
  (->
    (store/account-operations account-id)
    (logic/sort-operations-by-date)
    (logic/operations->periods-of-debt)
    (data->json)))
