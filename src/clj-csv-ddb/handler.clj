(ns clj-csv-ddb.handler
  (:use compojure.core)
  (:require [clj-csv-ddb.constants :as const]
            [clj-csv-ddb.aws-db :as ddb]
            [clojure.walk :as walk]
            [compojure.core :refer :all ]
            [compojure.route :as route]
            [org.httpkit.server :as httpkit :refer [run-server]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.params :refer [wrap-params]]))

(defn get-items!
  "Returns JSON with all maps or by query parameters."
  []
  (try
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (ddb/get-all!)}
    (catch Exception e
      (const/internal-server-error))))

(defn get-item-by-id!
  "Returns JSON map by ID."
  [id]
  (try
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (ddb/get-item! id)}
    (catch Exception e
      (const/internal-server-error))))

(defn delete-item-by-id!
  "Returns JSON of maps without the entry with id."
  [id]
  (try
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (ddb/delete-item! id)}
    (catch Exception e
      (const/internal-server-error))))

(defn edit-item-by-id!
  "Returns JSON with the entry with id modified by qp."
  [id qp]
  (try
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (ddb/update-item! id (walk/keywordize-keys qp))}
    (catch Exception e
      (const/internal-server-error))))

(defn add-item!
  "Returns JSON including a new element with k/v from qp."
  [qp]
  (try
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (ddb/add-item! qp)}
    (catch Exception e
      (const/internal-server-error))))

(defroutes app-routes ; define endpoints
  (GET "/" [] get-items!)
  (GET "/:id" [id] (get-item-by-id! id))
  (DELETE "/:id" [id] (delete-item-by-id! id))
  (PUT "/:id" [id :as {qp :query-params}] (edit-item-by-id! id qp))
  (POST "/" [:as {qp :query-params}] (add-item! qp))
  (route/not-found const/not-found))

(def app ; app settings
  (-> app-routes
      wrap-json-body ; converts return values to JSON
      wrap-json-response
      wrap-params
      (wrap-cors :access-control-allow-origin [#"http://localhost:8700"]
                 :access-control-allow-methods [:get :put :post :delete])))

(def start-app #(run-server app {:port 5000}))
