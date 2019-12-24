(ns balances.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [balances.handlers :as handlers]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(defroutes app-routes
  (context "/accounts/:account-id" [account-id]
           (POST  "/operation"       [] handlers/operation-handler)
           (GET   "/balance"         [] handlers/balance-handler)
           (GET   "/statement"       [] handlers/statement-handler)
           (GET   "/periods-of-debt" [] handlers/periods-of-debt-handler)))

(defn wrap-json-content-type
  "Set content type to JSON for all responses"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application-json"))))

(defn wrap-handler
  [handler]
  (->
    handler
    (wrap-json-content-type)
    (wrap-keyword-params)
    (wrap-params)))

(defn -main
  "Start the web server"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (server/run-server (wrap-handler app-routes) {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
