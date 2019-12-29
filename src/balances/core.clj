(ns balances.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [balances.handlers :as handlers]
            [balances.utils :refer :all]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

; unparse date to string
(extend-protocol cheshire.generate/JSONable
  org.joda.time.DateTime
  (to-json [dt gen]
    (cheshire.generate/write-string gen (unparse-date dt))))

(defroutes app-routes
  (context "/accounts/:account-id" [account-id]
           (POST  "/operation"       [] handlers/operation-handler)
           (GET   "/balance"         [] handlers/balance-handler)
           (GET   "/statement"       [] handlers/statement-handler)
           (GET   "/periods-of-debt" [] handlers/periods-of-debt-handler)))

(defn wrap-handler
  [handler]
  (->
    handler
    (wrap-keyword-params)
    (wrap-params)
    (wrap-json-body {:keywords? true})
    (wrap-json-response)))

(def app (wrap-handler app-routes))

(defn -main
  "Start the web server"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (server/run-server app {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
