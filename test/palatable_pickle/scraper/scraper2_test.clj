(ns palatable-pickle.scraper.scraper2-test 
  (:require [clojure.pprint :as pp]
            [clojure.test :as t]
            [palatable-pickle.all-recipes.constants :as constants]
            [palatable-pickle.scraper.scraper2 :as s2]))

(t/deftest test-get-page
  (t/testing "testing get page"
    (pp/pprint (s2/get-page (:home constants/urls)))))