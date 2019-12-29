(ns balances.logic-test
  (:require [clojure.test :refer :all]
            [balances.logic :as logic]
            [balances.utils :refer [parse-date]
                            :rename {parse-date d}]
            [balances.helpers :refer :all]))

(deftest calculate-balance-test
  (testing "It should calculate balance properly"
    (let [operations [(debit-operation "20/12/2019" 50)
                      (debit-operation "20/12/2019" 50)
                      (credit-operation "20/12/2019" 90)]
          balance (logic/calculate-balance operations)]
      (is (= balance 10))))

  (testing "It should return zero on empty"
    (let [balance (logic/calculate-balance [])]
      (is (= balance 0)))))

(deftest operations->groups-test
  (testing "It should group operations by date"
    (let [operations [(credit-operation "20/12/2019")
                      (credit-operation "20/12/2019")
                      (debit-operation "21/12/2019")]
          groups (logic/operations->groups operations)
          expected-groups {
            (d "20/12/2019") [
              {:type :credit :amount 20 :date (d "20/12/2019") :merchant "McDonalds"}
              {:type :credit :amount 20 :date (d "20/12/2019") :merchant "McDonalds"}
            ]
            (d "21/12/2019") [
              {:type :debit :amount 100 :date (d "21/12/2019")}
            ]
          }
        ]
      ; should return two groups
      (is (= groups expected-groups)))))

(deftest operations->statement-test
  (testing "It should generate statement for operations"
    (let [operations [(credit-operation "20/12/2019" 30)
                      (credit-operation "20/12/2019" 30)
                      (debit-operation "21/12/2019" 100)]
          statement (logic/operations->statement operations)
          expected-statement [
            {
              :date (d "20/12/2019")
              :operations [
                {:type :credit :amount 30 :date (d "20/12/2019") :merchant "McDonalds"}
                {:type :credit :amount 30 :date (d "20/12/2019") :merchant "McDonalds"}
              ]
              :balance -60
            }
            {
              :date (d "21/12/2019")
              :operations [
                {:type :debit :amount 100 :date (d "21/12/2019")}
              ]
              :balance 40
            }
          ]
        ]
      (is (= statement expected-statement)))))

(deftest operations->periods-of-debt-test
  (testing "It should properly calculate periods of debt"
    (let [operations [(credit-operation "1/11/2019" 10)
                      (credit-operation "1/11/2019" 10)
                      (debit-operation  "4/11/2019" 50)
                      (credit-operation "6/11/2019" 200)
                      (credit-operation "9/11/2019" 200)]
          periods-of-debt (logic/operations->periods-of-debt operations)
          expected-periods-of-debt [
            {
              :principal 20
              :start (d "01/11/2019")
              :end (d "03/11/2019")
            }
            {
              :principal 170
              :start (d "06/11/2019")
              :end (d "08/11/2019")
            }
            {
              :principal 370
              :start (d "09/11/2019")
            }
          ]
        ]
      (is (= periods-of-debt expected-periods-of-debt)))))

(deftest filter-statement-by-date-test
  (testing "It should filter statement by dates"
    (let [operations [(credit-operation "01/12/2019")
                      (credit-operation "02/12/2019")
                      (credit-operation "03/12/2019")
                      (credit-operation "04/12/2019")
                      (credit-operation "05/12/2019")
                      (credit-operation "06/12/2019")]
          statement (logic/operations->statement operations)
          filtered (logic/filter-statement-by-date
                     statement
                     (d "03/12/2019")
                     (d "05/12/2019"))
          expected-statement [
            {
              :date (d "03/12/2019")
              :operations [
                {:type :credit :amount 20 :date (d "03/12/2019") :merchant "McDonalds"}
              ]
              :balance -60
            }
            {
              :date (d "04/12/2019")
              :operations [
                {:type :credit :amount 20 :date (d "04/12/2019") :merchant "McDonalds"}
              ]
              :balance -80
            }
            {
              :date (d "05/12/2019")
              :operations [
                {:type :credit :amount 20 :date (d "05/12/2019") :merchant "McDonalds"}
              ]
              :balance -100
            }
          ]
        ]
      (is (= filtered expected-statement)))))

(deftest sort-operations-by-date-test
  (testing "It should return list sorted"
    (let [operations [(credit-operation "10/12/2019")
                      (credit-operation "02/12/2019")
                      (credit-operation "13/12/2019")
                      (credit-operation "04/12/2019")
                      (credit-operation "05/12/2019")
                      (credit-operation "06/12/2019")]
          sorted (logic/sort-operations-by-date operations)
          dates (map #(% :date) sorted)]
      (is (= (first dates) (d "02/12/2019")))
      (is (= (last dates) (d "13/12/2019"))))))
