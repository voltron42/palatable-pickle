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
    (let [page (scraper/scrape-all-recipes)
          links (scraper/get-links-from-page page)
          cat-links (reduce-kv
                     #(assoc %1 %2 (into (sorted-set) (filter (fn [link] (not (nil? (str/index-of link %3)))) links)))
                     {}
                     link-types)
          all-cat-links (reduce into (sorted-set) (vals cat-links))
          all-links (assoc cat-links :other (set/difference links all-cat-links))
          recipe (-> all-links :recipes first scraper/get-page)]
      (pp/pprint recipe)
      (pp/pprint all-links))))
