(ns clj-csv-ddb.aws-db
  (:require [clj-csv-ddb.csv :as csv]
            [clj-csv-ddb.util :as util]
            [clj-csv-ddb.constants :as const]
            [taoensso.faraday :as far]))

(defn list-tables!
  "List all tables."
  []
  (far/list-tables const/ddb))

(defn create-table!
  "Create the helmet-response table."
  []
  (far/create-table const/ddb :helmet-response
                    [:vector :s]
                    {:throughput {:read 1 :write 1}
                     :block? true}))

(defn delete-table!
  "Delete the helmet-response table."
  []
  (far/delete-table const/ddb :helmet-response))

(defn add-item!
  "Add an item to the helmet-response table by hash-map."
  [m]
  (far/put-item const/ddb
                :helmet-response
                m))

(defn get-all!
  "Retrieves all items from the helmet-response table."
  []
  (far/scan const/ddb :helmet-response))

(defn get-item!
  "Retrieves item by id (vector) from the helmet-response table."
  [id]
  (far/get-item const/ddb :helmet-response {:vector id}))

(defn delete-item!
  "Deletes an item by id (vector) from the helmet-response table."
  [id]
  (far/delete-item const/ddb :helmet-response {:vector id} {:return-cc? true}))

(defn update-item!
  "Edits an item in the helmet-response table by id (vector) and hash-map."
  [id m]
  (try
    (let [item (assoc m :vector id)
          item-parsed (util/values->ints item const/int-kws)]
      (far/put-item const/ddb
                    :helmet-response
                    item-parsed
                    {:cond-expr (str "vector = :vector")
                     :expr-attr-vals {":vector" id}}))
    (catch com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException e
      e)))

(defn populate-table!
  "Populates the helmet-response table with all the items in maps."
  [maps]
  (map #(add-item! %) maps))

(defn initialize!
  "Populates the helmet-response table with data from csv."
  []
  (populate-table! (util/maps-without-blanks (csv/helmet-responses!))))
