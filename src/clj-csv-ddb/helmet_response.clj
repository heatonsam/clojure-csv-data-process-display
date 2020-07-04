(ns clj-csv-ddb.helmet-response
  (:require
   [clojure.spec.alpha :as s]))

;; Spec

;; complex specification for hash-map validation

(s/def ::vector-pattern (s/and
                         string?
                         #(= (subs % 0 1) "v")
                         #(int? (Integer/parseInt (subs % 1)))))

(def coordinate-regex #"([0-9].[0-9].[0-9].[0-9])")

(s/def ::coordinate-pattern (s/and
                             string?
                             #(re-matches coordinate-regex %)))

; simple specification for hash-map validation

(s/def ::ref_date pos-int?)
(s/def ::geo string?)
(s/def ::sex string?)
(s/def ::age_group string?)
(s/def ::student_response string?)
(s/def ::uom string?)
(s/def ::uom_id nat-int?)
(s/def ::scalar_factor string?)
(s/def ::scalar_id nat-int?)
(s/def ::vector ::vector-pattern)
(s/def ::coordinate ::coordinate-pattern)
(s/def ::value nat-int?)
(s/def ::decimals nat-int?)

; specification for helmet-response maps

(s/def ::helmet-response (s/keys :req-un [::ref_date ::geo ::dguid ::sex ::age_group ::student_response ::uom ::uom_id ::scalar_factor ::scalar_id ::vector ::coordinate ::value ::status ::symbol ::terminated ::decimals]))

;; Functions

(defn helmet-response
  "Takes hash-map and returns hash-map validated as ::helmet-response or :clojure.spec.alpha/invalid."
  [m]
  (s/conform ::helmet-response m))
