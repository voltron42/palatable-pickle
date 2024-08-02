(ns palatable-pickle.driver-test 
  (:require [clojure.test :as t]
            [palatable-pickle.driver :as driver]))

(def driver (atom nil))

(defn close-driver [f]
  (f)
  (doto @driver
    (.close)
    (.quit)))

(t/use-fixtures :once close-driver)

(t/deftest test-driver
  (t/testing "testing driver"
    (reset! driver (driver/build-driver 4446))
    (t/is (not (nil? @driver)))
    (-> @driver (.get "https://www.google.com"))
    (t/is (= "Google" (.getTitle @driver)))))

