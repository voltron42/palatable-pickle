(ns palatable-pickle.driver-test 
  (:require [clojure.test :as t]
            [palatable-pickle.driver :as driver]))

(defn close-driver [f]
  (f)
  (driver/close-driver))

(t/use-fixtures :once close-driver)

(t/deftest ^:driver test-driver
  (t/testing "testing driver"
    (driver/set-page "https://www.google.com")
    (t/is (= "Google" (driver/get-title)))))

