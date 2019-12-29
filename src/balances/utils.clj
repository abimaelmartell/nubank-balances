(ns balances.utils
  (:require [clj-time.core :as t]
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
  (t/minus date (t/days 1)))

(defn map->operation
  [json]
  (-> json
      (update :date parse-date)
      (update :type keyword)))
