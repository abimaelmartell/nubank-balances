(ns balances.store-test
  (:require [clojure.test :refer :all]
            [balances.store :refer :all]
            [balances.helpers :refer :all]))

(deftest save-operation!-test
  (testing "It should add to accounts"
    (reset-accounts!)
    (is (= (account-operations "1") nil))

    (save-operation! "1" credit-operation)
    (save-operation! "1" credit-operation)

    (is (= (count (account-operations "1")) 2))))
