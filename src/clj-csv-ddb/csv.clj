(ns clj-csv-ddb.csv
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clj-csv-ddb.constants :as const]
   [clj-csv-ddb.helmet-response :as hr]
   [clj-csv-ddb.util :as util]))

;; i/o functions

(defn- csv-data!
  "Reads in and return csv data from filepath."
  [filepath]
  (with-open [in-file (io/reader filepath)]
    (doall
     (csv/read-csv in-file))))

(defn- csv-data->maps!
  "Takes csv data and return seq of maps."
  [csv-data]
  (map zipmap
       (->> (first csv-data)
            (map util/replace-and-lowercase)
            (map keyword)
            repeat)
	  (rest csv-data)))

(defn maps->csv!
  "Persists a validated seq of ::helmet-response maps to filepath and returns the seq."
  [data filepath]
  (let [validated-maps (map #(hr/helmet-response %) data) ; validate maps before persisting
        columns (keys (first validated-maps))
        headers (map name columns)
        rows (mapv #(mapv % columns) validated-maps)]
    (with-open [writer (io/writer filepath)]
      (csv/write-csv writer (cons headers rows))
      validated-maps)))

(defn helmet-responses!
  "Return parsed and validated ::helmet-response maps from csv file."
  []
  (->> (csv-data! const/dataset)
   csv-data->maps! ; apply to above return to get maps
   (map #(util/values->ints % const/int-kws)) ; same and parse ints
   (map #(hr/helmet-response %)))) ; validate maps
