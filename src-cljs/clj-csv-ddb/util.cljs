(ns clj-csv-ddb.util
  (:require [clojure.string :as str]
            [goog.string :as gstring]))

(defn sorted-contents
  "Returns the data sorted by sort-state."
  [sort-state data]
  (let [sorted-contents (sort-by (:sort-val sort-state) data)]
    (if (:ascending sort-state)
      sorted-contents
      (reverse sorted-contents))))

(defn filter-content
  "Returns data in table-state where it matches filter-string."
  [filter-string table-state]
  (filter #(re-find (->> (str filter-string)
                         (str/upper-case)
                         (re-pattern))
                    (str/upper-case (str (vals %)))) table-state))

(defn maps-except-item
  "Returns a seq of maps excluding the element with key id."
  [id maps]
  (remove #(= id (:vector %)) maps)) ; vector = id

(defn maps-filtered-by-id
  "Filters a seq of maps by key of an entry."
  [id maps]
  (filter #(= id (:vector %)) maps)) ; vector = id

(defn replace-item
  "Returns seq of maps with element replaced with item, by id."
  [item maps]

  (let [maps-without (maps-except-item (:vector item) maps) ; vector = id
        maps-with (conj maps-without item)]
    maps-with))

(defn editable?
  "Returns true if row contains key :editable."
  [row]
  (= (:editable row) true))

(defn none-editable
  "Returns seq of maps with :editable removed from all hash-maps."
  [table-state]
  (map #(dissoc % :editable) table-state))

(defn maps-filtered-by-value
  "Filters a seq of maps by key of an entry."
  [k v maps]
  (filter #(= v (k %)) maps))

(defn data-age-filter
  "Returns seq of maps filtered by the values in age."
  [table-state age]
  (map #(maps-filtered-by-value :age_group % table-state) age))

(defn data-gender-filter
  "Returns seq of maps filtered by the values in gender."
  [table-state gender]
  (map #(maps-filtered-by-value :sex % table-state) gender))

(defn pct
  "Return percentage up to 2 decimal places."
  [p t]
  (let [p (/ (* p 100.0) t)]
    (if (js/isNaN p)
      0
      (gstring/format "%.2f" p))))

(defn label
  "Returns a label comprised of s and a percentage value from h-state."
  [s k h-state]
  (str s " (" (k h-state) "%)"))

(defn genders
  "Returns the gender values present in t."
  [t]
  (set (map #(get % :sex) @t)))

(defn ages
  "Returns the age values present in t."
  [t]
  (set (map #(get % :age_group) @t)))
