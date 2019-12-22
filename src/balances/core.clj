(ns balances.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [balances.handlers :as handlers]))

(defroutes app-routes
  (context "/accounts/:account-id" [account-id]
           (POST  "/operation"       [account-id] handlers/operation-handler)
           (GET   "/balance"         [account-id] handlers/balance-handler)
           (GET   "/statement"       [account-id] handlers/statement-handler)
           (GET   "/periods-of-debt" [account-id] handlers/periods-of-debt-handler)))

(defn json-middleware
  "Set content type to JSON for all responses"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application-json"))))

(defn -main
  "Start the web server"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (server/run-server (json-middleware app-routes) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
