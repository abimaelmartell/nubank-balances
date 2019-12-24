(ns balances.validations-test
  (:require [clojure.test :refer :all]
            [balances.validations :refer :all]
            [balances.helpers :refer :all]))

(deftest is-valid-date?-test
  (testing "It should return false on valid date"
    (is (not (is-valid-date? "50/12/2019"))))

  (testing "It should return true on valid date"
    (is (is-valid-date? "10/12/2019"))))

(deftest validate-missing-keys-test
  (testing "It should return missing keys for credit"
    (let [missing-keys (validate-missing-keys { :type "credit" })]
      (is (= missing-keys #{:amount :date :merchant}))))
  (testing "It should return missing keys for debit"
    (let [missing-keys (validate-missing-keys { :type "debit" })]
      (is (= missing-keys #{:amount :date}))))

  (testing "It should return missing keys for default"
    (let [missing-keys (validate-missing-keys {})]
      (is (= missing-keys #{:amount :date :type})))))

(deftest validate-operation-data-test
  (testing "It should return keys for invalid data"
    (let [invalid (validate-operation-data { :type "debit" :amount "20" :date "20122019" })]
      (is (= (set invalid) #{"amount" "date"})))
    ; amount should be numeric
    (let [invalid (validate-operation-data { :type "debit" :amount "20" :date "12/12/2019" :merchant "McDonalds" })]
      (is (= (set invalid) #{"amount"})))
    ; amount should be positive
    (let [invalid (validate-operation-data { :type "debit" :amount -20 :date "12/12/2019" :merchant "McDonalds" })]
      (is (= (set invalid) #{"amount"})))
    ; date should be in dd/mm/YYYY
    (let [invalid (validate-operation-data { :type "debit" :amount 20 :date "12122019" :merchant "McDonalds" })]
      (is (= (set invalid) #{"date"})))
    ; type should be debit or credit
    (let [invalid (validate-operation-data { :type "otro" :amount 20 :date "12/12/2019" :merchant "McDonalds" })]
      (is (= (set invalid) #{"type"})))
    ; merchant should be string
    (let [invalid (validate-operation-data { :type "debit" :amount 20 :date "12/12/2019" :merchant 100 })]
      (is (= (set invalid) #{"merchant"}))))

  (testing "It should return empty on valid data"
    (let [invalid (validate-operation-data { :type "debit" :amount 20 :date "12/12/2019" })]
      (is (empty? invalid)))))

(deftest validate-operation-test
  (testing "It should return nil on valid operation"
    (let [result (validate-operation (debit-operation-json))]
      (is (nil? result))))

  (testing "It should return string on error"
    (let [result (validate-operation (debit-operation-json "20122012"))]
      (is (= result "Invalid data on: date")))

    (let [result (validate-operation (debit-operation-json "12/12/2019" "90"))]
      (is (= result "Invalid data on: amount")))

    (let [result (validate-operation {:type "credit"})]
      (is (= result "Missing keys: amount, date, merchant")))))
