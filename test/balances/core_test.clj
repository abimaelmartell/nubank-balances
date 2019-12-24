(ns balances.core-test
  (:require [clojure.test :refer :all]
            [balances.core :refer :all]
            [ring.mock.request :as mock]))

(deftest app-test
  (testing "It should always respond with json"
    (let [response (app (-> (mock/request :get "/account/1/statement")))
          headers (response :headers)]
      (is (= "application/json" (get headers "Content-Type"))))))
