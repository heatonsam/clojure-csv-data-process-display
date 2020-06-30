(ns assignment4.test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [assignment4.csv :as csv]
            [assignment4.helmet-response :as hr]))

(deftest test-app
  (testing "helmet-response!"
    (let [response-maps (csv/helmet-responses!)]
      (doseq [x response-maps]
        (is (s/valid? ::hr/helmet-response x))))))
