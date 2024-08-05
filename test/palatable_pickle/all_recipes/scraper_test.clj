(ns palatable-pickle.all-recipes.scraper-test 
  (:require [clojure.pprint :as pp]
            [clojure.test :as t]
            [palatable-pickle.all-recipies.scraper :as scraper]
            [palatable-pickle.driver :as driver]
            [clojure.string :as str]))

(defn close-driver [f]
  (f)
  (driver/close-driver))

(t/use-fixtures :once close-driver)

(t/deftest ^:scraper test-get-page
  (t/testing "test get-page"
    (let [page (scraper/scrape-all-recipes)
          links (scraper/get-links-from-page page)
          recipes (filter #(not (nil? (str/index-of % "/recipe/"))) links)
          recipe (scraper/get-page (first recipes))]
      (pp/pprint (dissoc recipe :menu :breadcrumb :recipe-details :recipe-steps :article-title :calories :servings :nutritional-facts)))))
