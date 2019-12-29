(ns balances.core-test
  (:require [clojure.test :refer :all]
            [balances.core :refer :all]
            [ring.mock.request :as mock]))

(deftest app-test
  (testing "It should always respond with json"
    (let [response (app (-> (mock/request :get "/accounts/1/statement")))
          headers (response :headers)]
      (is (= "application/json; charset=utf-8" (get headers "Content-Type"))))))
