(ns assignment4.tests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [assignment4.core :as core]
            [assignment4.helmet_response :as hr]
            [cljs.core.async :as async :refer [<!]]
            [clojure.spec.alpha :as s]))

;; Validates the results of helmet-responses maps according to the helmet-response spec.

(deftest test-app
  (testing "helmet-response!"
    (go (let [response-maps (<! (core/get-response! {}))]
          (doseq [x response-maps]
            (println (s/valid? ::hr/helmet-response x))
            (is (s/valid? ::hr/helmet-response x)))))))

(run-tests)
