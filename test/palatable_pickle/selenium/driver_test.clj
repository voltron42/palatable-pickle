(ns palatable-pickle.selenium.driver-test 
  (:require [clojure.test :as t]
            [palatable-pickle.selenium.driver :as driver]))

(t/deftest ^:driver test-driver
  (t/testing "testing driver"
    (driver/using-browser
     #(do
        (driver/set-page % "https://www.google.com")
        (t/is (= "Google" (driver/get-title %)))))))

