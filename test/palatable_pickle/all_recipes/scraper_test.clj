(ns palatable-pickle.all-recipes.scraper-test 
  (:require [clojure.pprint :as pp]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as t]
            [palatable-pickle.all-recipies.scraper :as scraper]
            [palatable-pickle.driver :as driver]))

(defn close-driver [f]
  (f)
  (driver/close-driver))

(t/use-fixtures :once close-driver)

(def link-types
  {:recipes "/recipe/"
   :recipe-lists "/recipes/"
   :articles "/article/"
   :trends "/food-news-trends/"
   :galleries "/gallery/"
   :kitchen-tips "/kitchen-tips/"})

(t/deftest ^:scraper test-get-page
  (t/testing "test get-page"
    (let [{next-links :next-links pages :pages} (scraper/scrape-all-recipes)]
      (pp/pprint {:links (count next-links)
                  :pages (count pages)}))))
