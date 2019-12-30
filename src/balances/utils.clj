(ns balances.utils
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(def date-formatter (f/formatter "dd/MM/yyyy"))

(defn parse-date
  "Return date object from string"
  [date]
  (f/parse date-formatter date))

(defn unparse-date
  "Return string rom date object"
  [date]
  (f/unparse date-formatter date))

(defn sort-by-date
  "Sorting function for dates"
  [a b]
  (t/before? (a :date) (b :date)))

(defn minus-one-day
  "Rest one day to date"
  [date]
  (t/minus date (t/days 1)))

(defn map->operation
  "Convert map to operation data"
  [map]
  (-> map
      (update :date parse-date)
      (update :type keyword)))
