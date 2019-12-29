(ns balances.validations
  (:require [balances.utils :refer :all]
            [clj-time.core :as t])
  (:use [clojure.set :only [difference]]
        [clojure.string :only (join)]))

(def valid-operation-types
  #{"debit" "credit"})

(def debit-operation-keys
  #{:type :amount :date})

(def credit-operation-keys
  #{:type :amount :date :merchant})

(defn operation-keys
  [type]
  (if (= type "credit")
    credit-operation-keys
    debit-operation-keys))

(defn validate-missing-keys
  [operation]
  (difference
    (operation-keys (operation :type))
    (keys operation)))

(defn is-valid-date?
  [date]
  (try
    (parse-date date) true
    (catch Exception e false)))

(defn validate-operation-data
  [operation]
  (reduce
    (fn [errors [key value]]
      (if-not (case key
                :amount (and (number? value) (pos? value))
                :type (contains? valid-operation-types value)
                :date (is-valid-date? value)
                :merchant (string? value)
                false)
        (conj errors (name key))
        errors))
    []
    operation))

(defn validate-operation
  [operation]
  (let [missing-keys (validate-missing-keys operation)
        invalid-data (validate-operation-data operation)]
    (cond
      (not (empty? missing-keys))
        (str "Missing keys: " (join ", " (map name missing-keys)))

      (not (empty? invalid-data))
        (str "Invalid data on: " (join ", " invalid-data)))))

(defn are-valid-statement-dates?
  [starting ending]
  (if (and (is-valid-date? starting)
           (is-valid-date? ending))
    (let [date-starting (parse-date starting)
          date-ending (parse-date ending)]
      (t/before? date-starting date-ending))
    false))
