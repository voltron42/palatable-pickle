(ns palatable-pickle.all-recipes.constants-test 
  (:require [clojure.pprint :as pp]
            [clojure.test :as t]) 
  (:import [org.openqa.selenium By]))

(t/deftest test-by
  (t/testing "testing by types"
    (let [class-name (By/className "show")
          tag-name (By/tagName "p")
          x-path (By/xpath "//button[contains(text(),'Submit')]")]
      (pp/pprint {:className {:by class-name
                              :toString (str class-name)}
                  :tagName {:by tag-name
                            :toString (str tag-name)}
                  :xpath {:by x-path
                          :toString (str x-path)}}))))