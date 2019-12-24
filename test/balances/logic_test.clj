(ns balances.logic-test
  (:require [clojure.test :refer :all]
            [balances.logic :as logic]
            [balances.utils :as utils]
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
          groups (logic/operations->groups operations)]
      ; should return two groups
      (is (= (count groups) 2))
      ; first group key should be first date
      (is (= (first (first groups)) "20/12/2019"))
      ; first group operations should be two
      (is (= (count (second (first groups))) 2))
      ; second group key should be second date
      (is (= (first (second groups)) "21/12/2019"))
      ; second group operations should be one
      (is (= (count (second (second groups))) 1)))))

(deftest operations->statement-test
  (testing "It should generate statement for operations"
    (let [operations [(credit-operation "20/12/2019" 30)
                      (credit-operation "20/12/2019" 30)
                      (debit-operation "21/12/2019" 100)]
          statement (logic/operations->statement operations)]

      ; should return two dates
      (is (= (count statement) 2))

      ; first one should be earlier date
      (is (= (first (first statement)) "20/12/2019"))
      ; first date contains two operations
      (is (= (count (get (second (first statement)) :operations)) 2))
      ; balance at first date is -60
      (is (= (get (second (first statement)) :balance) -60))

      ; second date should be latest date
      (is (= (first (second statement)) "21/12/2019"))
      ; second date contains one operation
      (is (= (count (get (second (second statement)) :operations)) 1))
      ; second date balance is 40
      (is (= (get (second (second statement)) :balance) 40)))))

(deftest operations->periods-of-debt-test
  (testing "It should properly calculate periods of debt"
    (let [operations [(credit-operation "1/11/2019" 10)
                      (credit-operation "1/11/2019" 10)
                      (debit-operation  "4/11/2019" 50)
                      (credit-operation "6/11/2019" 200)
                      (credit-operation "9/11/2019" 200)]
          periods-of-debt (logic/operations->periods-of-debt operations)]

      ; should generate three periods of debt
      (is (= (count periods-of-debt) 3))

      ; first period
      (let [[first-period] periods-of-debt]
        ; should start on first credit operation
        (is (= (first-period :start) "01/11/2019"))
        ; principal should be absolute negative balance of day
        (is (= (first-period :principal) 20))
        ; end day should be one day before next operation
        (is (= (first-period :end) "03/11/2019")))

      ; second period
      (let [second-period (second periods-of-debt)]
        ; should start on next credit operation
        (is (= (second-period :start) "06/11/2019"))
        ; principal should be absolute of balance at this time
        (is (= (second-period :principal) 170))
        ; end day should be one day before next operation
        (is (= (second-period :end) "08/11/2019")))

      ; second period
      (let [third-period (last periods-of-debt)]
        ; should start on next credit operation
        (is (= (third-period :start) "09/11/2019"))
        ; principal should be absolute of balance at this time
        (is (= (third-period :principal) 370))
        ; current balance is negative, so end is nil
        (is (= (third-period :end) nil))))))

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
                     (utils/parse-date "03/12/2019")
                     (utils/parse-date "05/12/2019"))]
      (is (= (count filtered) 3))

      (is (= "03/12/2019" (first (first filtered))))
      (is (= "04/12/2019" (first (second filtered))))
      (is (= "05/12/2019" (first (last filtered)))))))

(deftest sort-operations-by-date-test
  (testing "It should return list sorted"
    (let [operations [(credit-operation "10/12/2019")
                      (credit-operation "02/12/2019")
                      (credit-operation "13/12/2019")
                      (credit-operation "04/12/2019")
                      (credit-operation "05/12/2019")
                      (credit-operation "06/12/2019")]
          sorted (logic/sort-operations-by-date operations)
          dates (map #(utils/unparse-date (% :date)) sorted)]
      (is (= (first dates) "02/12/2019"))

      (is (= (last dates) "13/12/2019")))))
