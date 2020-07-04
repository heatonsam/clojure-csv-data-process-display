(ns clj-csv-ddb.constants)

(def dataset "13100262.csv") ; path to csv file

(def int-kws [:ref_date :uom_id :scalar_id :value :decimals]) ; keywords designating ints

(def not-found
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "404 not found"})

(def internal-server-error
  {:status 500
   :headers {"Content-Type" "text/plain"}
   :body "500 Internal Server Error"})

(def ddb {:access-key ""
          :secret-key ""})
