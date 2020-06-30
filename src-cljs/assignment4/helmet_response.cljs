(ns assignment4.helmet_response
  (:require [clojure.spec.alpha :as s]))

(s/def ::vector-pattern (s/and
                         string?
                         #(= (subs % 0 1) "v")
                         #(int? (js/parseInt (subs % 1)))))

(def coordinate-regex #"([0-9].[0-9].[0-9].[0-9])")

(s/def ::coordinate-pattern (s/and
                             string?
                             #(re-matches coordinate-regex %)))

; simple specification for hash-map validation

(s/def ::ref_date pos-int?)

(s/def ::geo (s/nilable string?))
(s/def ::sex (s/nilable string?))
(s/def ::age_group (s/nilable string?))
(s/def ::student_response string?)
(s/def ::uom string?)
(s/def ::uom_id nat-int?)
(s/def ::scalar_factor string?)
(s/def ::scalar_id nat-int?)
(s/def ::vector ::vector-pattern)
(s/def ::coordinate (s/nilable ::coordinate-pattern))
(s/def ::value (s/nilable nat-int?))
(s/def ::decimals nat-int?)

; specification for helmet-response maps

(s/def ::helmet-response (s/keys :opt-un [::ref_date ::geo ::dguid ::sex ::age_group ::student_response ::uom ::uom_id ::scalar_factor ::scalar_id ::vector ::coordinate ::value ::status ::symbol ::terminated ::decimals ::editable]))

(def hr-keys-columns (array-map :ref_date "Reference date" :geo "Country" :dguid "DGUID" :sex "Sex" :age_group "Age group" :student_response "Student response" :uom "UOM" :uom_id "UOM ID" :scalar_factor "Scalar factor" :scalar_id "Scalar ID" :vector "Vector" :coordinate "Coordinate" :value "Value" :status "Status" :symbol "Symbol" :terminated "Terminated" :decimals "Decimals"))

(def int-kws [:ref_date :uom_id :scalar_id :value :decimals]) ; keywords designating ints

(defn helmet-response
  "Takes hash-map and returns hash-map validated as ::helmet-response or :clojure.spec.alpha/invalid."
  [m]
  (s/conform ::helmet-response m))
