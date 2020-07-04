(ns clj-csv-ddb.util
  (:require [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.edn :as edn]))

;; Pure functions

(defn bigint->int
  ""
  [m k]
  (update m k #(int %)))

(defn bigints->ints
  ""
  [m kws]
  (reduce bigint->int m kws))

(defn value->int
  "Takes hash-map and keyword and returns hash-map with the value of keyword parsed as int."
  [m k]
  (update m k #(edn/read-string %)))

(defn values->ints
  "Takes hash-map and returns hash-map with the values of keys in kws converted to ints."
  [m kws]
  (reduce value->int m kws))

(defn int->string
  "Takes hash-map and keyword and returns hash-map with the value of keyword parsed as int."
  [m k]
  (update m k #(str %)))

(defn ints->strings
  "Takes hash-map and returns hash-map with the values of keys in kws converted to ints."
  [m kws]
  (reduce int->string m kws))

(defn blank->nil
  "Takes a hash-map and a keyword and returns a hash-map with blank strings replaced by nil."
  [m k]
  (update m k #(if (= % "") nil %)))

(defn replace-and-lowercase
  "Takes string and returns lowercase String with spaces replaced by underscores."
  [string]
  (-> string
      (str/lower-case)
      (str/replace #"( )+" "_")))

(defn remove-blanks
  "Takes a seq of hash-maps and returns a seq of hash-maps with blank values removed."
  [maps]
  (apply merge (for [[k v] maps :when (or (and (string? v) (not (str/blank? v))) (int? v))] {k v})))

(defn maps-without-blanks
  [seq-of-maps]
  (->> seq-of-maps
       (map #(remove-blanks %))))

(defn maps-filtered-by-map
  "Filters a seq of maps by the keys/values in a map. Eliminates keys-vals in query-map not present in maps."
  [query-map maps operator]
  (filter #(operator query-map (select-keys % (keys query-map))) maps))

(defn maps-filtered-by-id
  "Filters a seq of maps by key of an entry."
  [id maps]
  (filter #(= id (:vector %)) maps)) ; vector = id

(defn maps-except-item
  "Returns a seq of maps excluding the element with key id."
  [id maps]
  (remove #(= id (:vector %)) maps)) ; vector = id

(defn replace-item
  "Returns seq of maps with element replaced with item, by id."
  [item maps]
  (let [maps-without (maps-except-item (:vector item) maps)] ; vector = id
    (conj maps-without item)))

(defn maps-with-changed-item
  "Returns a seq of maps with element identified by id modified by query-map"
  [id maps query-map]
  (let [args (walk/keywordize-keys query-map)
        element (into {} (maps-filtered-by-id id maps))
        allowed-args (select-keys args (keys (dissoc element :vector))) ; vector = primary key
        new-element (merge element allowed-args)]
    (replace-item new-element maps)))

(defn maps-plus-item
  "Returns a seq of map with a new element with query-map parameters. Eliminates keys-vals in query-map not present in maps."
  [maps query-map]
  (let [args (walk/keywordize-keys query-map)
        last-id (last (sort (map #(Integer/parseInt (subs (:vector %) 1)) maps)))
        last-map (into {} (maps-filtered-by-id (str "v" last-id) maps))
        new-id (str "v" (inc last-id))
        all-keys (keys (dissoc last-map :vector)) ; vector = primary key
        allowed-args (select-keys args all-keys)
        empty-map (assoc (reduce into {} (map #(assoc nil % "") all-keys)) :vector new-id)
        new-map (merge empty-map allowed-args)] ; validates new map
    (conj maps new-map)))
