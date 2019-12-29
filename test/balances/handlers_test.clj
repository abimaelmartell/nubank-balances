(ns balances.handlers-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [balances.core :refer [app]]
            [balances.helpers :refer :all]
            [balances.store :refer [account-operations reset-accounts!]]))

(defn post-operation
  [operation-json]
  (app (-> (mock/request :post "/accounts/1/operation")
           (mock/content-type "application/json")
           (mock/body (operation->json operation-json)))))

(deftest operation-handler-test
  (testing "It should run validations"
    (let [operation (credit-operation-json "202020")
          response (post-operation operation)
          body (parse-json (:body response))]

      (is (= (body :success) false))
      (is (= (body :error) "Invalid data on: date"))))

  (testing "It should add an operation"
    (let [stored-operations (account-operations "1")]
      ; start with empty
      (is (= (count stored-operations) 0)))
    (let [operation (credit-operation-json)
          response (post-operation operation)
          body (parse-json (:body response))
          stored-operations (account-operations "1")]

      (is (= (body :success) true))
      (is (= (count stored-operations) 1)))))

(deftest balance-handler-test
  (testing "It should return the balance"
    (reset-accounts!)
    (let [response (app (mock/request :get "/accounts/1/balance"))
          body (parse-json (response :body))]
      ; initial return zero
      (is (= (body :balance) 0)))

    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (debit-operation-json "12/12/2019" 350))
    (let [response (app (mock/request :get "/accounts/1/balance"))
          body (parse-json (response :body))]
      ; - 50 - 50 + 350 = 250
      (is (= (body :balance) 250)))

    (post-operation (credit-operation-json "12/12/2019" 500))
    (let [response (app (mock/request :get "/accounts/1/balance"))
          body (parse-json (response :body))]
      ; 250 - 500  = -250
      (is (= (body :balance) -250)))))

(deftest statement-handler-test
  (testing "It should return the statement"
    (reset-accounts!)
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (debit-operation-json "20/12/2019" 350))

    (let [response (app (mock/request :get "/accounts/1/statement"))
          statement (parse-json (:body response))
          expected-statement [
            {
              :date "12/12/2019"
              :operations [
                {:type "credit", :amount 50, :date "12/12/2019", :merchant "McDonalds"}
                {:type "credit", :amount 50, :date "12/12/2019", :merchant "McDonalds"}
              ]
              :balance -100
            }
            {
              :date "20/12/2019"
              :operations [
                {:type "debit", :amount 350, :date "20/12/2019"}
              ]
              :balance 250
            }
          ]
        ]
      (is (= statement expected-statement))))

  (testing "It should be able to filter by date"
    (reset-accounts!)
    (post-operation (credit-operation-json "01/12/2019" 50))
    (post-operation (credit-operation-json "02/12/2019" 50))
    (post-operation (credit-operation-json "03/12/2019" 50))
    (post-operation (credit-operation-json "04/12/2019" 50))
    (post-operation (credit-operation-json "05/12/2019" 50))
    (post-operation (credit-operation-json "06/12/2019" 50))
    (post-operation (credit-operation-json "07/12/2019" 50))

    (let [url "/accounts/1/statement?starting=03/12/2019&ending=05/12/2019"
          response (app (mock/request :get url))
          statement (parse-json (:body response :body))
          expected-statement [
             {
              :date "03/12/2019"
              :operations [
                { :type "credit" :amount 50 :date "03/12/2019" :merchant "McDonalds" }
              ]
              :balance -150
            }
             {
              :date "04/12/2019"
              :operations [
                { :type "credit" :amount 50 :date "04/12/2019" :merchant "McDonalds" }
              ]
              :balance -200
            }
             {
              :date "05/12/2019"
              :operations [
                { :type "credit" :amount 50 :date "05/12/2019" :merchant "McDonalds" }
              ]
              :balance -250
            }
          ]
        ]
      (is (= statement expected-statement)))))

(deftest periods-of-debt-handler-test
  (testing "It should return the statement"
    (reset-accounts!)
    (post-operation (credit-operation-json "10/12/2019" 50))
    (post-operation (credit-operation-json "10/12/2019" 50))
    (post-operation (credit-operation-json "15/12/2019" 350))
    (post-operation (credit-operation-json "15/12/2019" 350))
    (post-operation (debit-operation-json "20/12/2019" 1000))
    (post-operation (credit-operation-json "25/12/2019" 1000))

    (let [response (app (mock/request :get "/accounts/1/periods-of-debt"))
          periods-of-debt (parse-json (response :body))
          expected-periods-of-debt [
            {
              :principal 100
              :start "10/12/2019"
              :end "14/12/2019"
            }
            {
              :principal 800
              :start "15/12/2019"
              :end "19/12/2019"
            }
            {
              :principal 800
              :start "25/12/2019"
            }
          ]
        ]
      (is (= periods-of-debt expected-periods-of-debt)))))
