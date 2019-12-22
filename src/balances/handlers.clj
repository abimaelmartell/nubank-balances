(ns balances.handlers
  (:require [balances.store :as store]
            [balances.logic :as logic]
            [balances.utils :as utils]))

(defn operation-handler
  [req]
  (let [{ :keys [body params] } req
        { account-id :account-id } params]
    (store/save-operation! account-id
                           (->
                             (slurp body)
                             (utils/json->data)))
    (->>
      (store/account-operations account-id)
      (sort utils/sort-by-date)
      (utils/data->json))))

(defn balance-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->>
      (store/account-operations account-id)
      (logic/calculate-balance)
      (hash-map :balance)
      (utils/data->json))))

(defn statement-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->
      (store/account-operations account-id)
      (logic/operations->statement)
      (utils/data->json))))

(defn periods-of-debt-handler
  [req]
  (let [{{ account-id :account-id } :params } req]
    (->
      (store/account-operations account-id)
      (logic/operations->periods-of-debt)
      (utils/data->json))))
