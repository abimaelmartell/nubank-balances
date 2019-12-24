(ns balances.utils
  (:require
    [clojure.data.json :as json]
    [clj-time.core :as t]
    [clj-time.format :as f]))

(def date-formatter (f/formatter "dd/MM/yyyy"))

(defn parse-date
  [date]
  (f/parse date-formatter date))

(defn unparse-date
  [date]
  (f/unparse date-formatter date))

(defn sort-by-date
  [a b]
  (t/before? (a :date) (b :date)))

(defn minus-one-day
  [date]
  (unparse-date (t/minus (parse-date date) (t/days 1))))

(defn operation-map-reader
  [key value]
  (case key
    :type (keyword value)
    :date (parse-date value)
    value))

(defn operation-map-writer
  [key value]
  (if (= (type value) org.joda.time.DateTime)
    (f/unparse date-formatter value)
    value))

(defn data->json
  [data]
  (json/write-str data :value-fn operation-map-writer))

(defn parse-json
  [json]
  (json/read-str json :key-fn keyword))

(defn map->operation
  [json]
  (-> json
      (update :date parse-date)
      (update :type keyword)))
