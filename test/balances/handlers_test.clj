(ns balances.handlers-test
  (:require [clojure.test :refer :all]
            [balances.core :refer :all]
            [balances.helpers :refer :all]
            [ring.mock.request :as mock]
            [balances.utils :refer :all]
            [balances.store :refer [account-operations reset-accounts!]]))

(defn post-operation
  [operation-json]
  (app (-> (mock/request :post "/accounts/1/operation")
           (mock/content-type "application/json")
           (mock/body (data->json operation-json)))))

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
          body (parse-json (:body response :body))]
      (is (= (body :balance) 0)))
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (debit-operation-json "12/12/2019" 350))
    (let [response (app (mock/request :get "/accounts/1/balance"))
          body (parse-json (:body response :body))]
      (is (= (body :balance) 250)))
    (post-operation (credit-operation-json "12/12/2019" 500))
    (let [response (app (mock/request :get "/accounts/1/balance"))
          body (parse-json (:body response :body))]
      (is (= (body :balance) -250)))))


(deftest statement-handler-test
  (testing "It should return the statement"
    (reset-accounts!)
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (credit-operation-json "12/12/2019" 50))
    (post-operation (debit-operation-json "20/12/2019" 350))

    (let [response (app (mock/request :get "/accounts/1/statement"))
          statement (parse-json (:body response :body))]

      ; should return two dates
      (is (= (count statement) 2))

      ; first one should be earlier date
      (is (= (first (first statement)) (keyword "12/12/2019")))
      ; first date contains two operations
      (is (= (count (get (second (first statement)) :operations)) 2))
      ; balance at first date is -100
      (is (= (get (second (first statement)) :balance) -100))

      ; second date should be latest date
      (is (= (first (second statement)) (keyword "20/12/2019")))
      ; second date contains one operation
      (is (= (count (get (second (second statement)) :operations)) 1))
      ; second date balance is 250
      (is (= (get (second (second statement)) :balance) 250))))

  (testing "It should be able to filter by date"
    (reset-accounts!)
    (post-operation (credit-operation-json "01/12/2019" 50))
    (post-operation (credit-operation-json "02/12/2019" 50))
    (post-operation (credit-operation-json "03/12/2019" 50))
    (post-operation (credit-operation-json "04/12/2019" 50))
    (post-operation (credit-operation-json "05/12/2019" 50))
    (post-operation (credit-operation-json "06/12/2019" 50))
    (post-operation (credit-operation-json "07/12/2019" 50))

    (let [response (app (mock/request :get "/accounts/1/statement?starting=03/12/2019&ending=05/12/2019"))
          statement (parse-json (:body response :body))]

      ; should return two dates
      (is (= (count statement) 3))

      ; first one should be earlier date
      (is (= (first (first statement)) "03/12/2019"))
      ; first date contains two operations
      (is (= (count (get (second (first statement)) :operations)) 1))
      ; balance at first date is -100
      (is (= (get (second (first statement)) :balance) -150))

      ; second date should be latest date
      (is (= (first (second statement)) "04/12/2019"))
      ; second date contains one operation
      (is (= (count (get (second (second statement)) :operations)) 1))
      ; second date balance is 250
      (is (= (get (second (second statement)) :balance) -200))

      ; second date should be latest date
      (is (= (first (statement 2)) "05/12/2019"))
      ; second date contains one operation
      (is (= (count (get (second (statement 2)) :operations)) 1))
      ; second date balance is 250
      (is (= (get (second (statement 2)) :balance) -250)))))


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
          periods-of-debt (parse-json (:body response :body))]

      (is (= ((periods-of-debt 0) :principal) 100))
      (is (= ((periods-of-debt 0) :start) "10/12/2019"))
      (is (= ((periods-of-debt 0) :end) "14/12/2019"))

      (is (= ((periods-of-debt 1) :principal) 800))
      (is (= ((periods-of-debt 1) :start) "15/12/2019"))
      (is (= ((periods-of-debt 1) :end) "19/12/2019"))

      (is (= ((periods-of-debt 2) :principal) 800))
      (is (= ((periods-of-debt 2) :start) "25/12/2019"))
      (is (= ((periods-of-debt 2) :end) nil)))))
