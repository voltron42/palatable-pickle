(ns palatable-pickle.driver-test 
  (:require [clojure.test :as t]
            [palatable-pickle.driver :as driver]))

(t/deftest test-driver
  (t/testing "Ensuring driver is not null"
    (let [driver (:driver (driver/build-driver))]
      (t/is (not-empty driver))
      (-> driver (.get "https://www.google.com"))
      (t/is (= "Google" (.getTitle driver))))))
