(ns palatable-pickle.all-recipies.scraper 
  (:require [palatable-pickle.all-recipies.constants :as constants]
            [palatable-pickle.driver :as driver]) 
  (:import [org.openqa.selenium NoSuchElementException]))

(defmulti parse-map #(set (keys %2)))

(defmulti parse-item #(type %2))

(defn parse-page [searcher queries]
  (reduce-kv
   (fn [acc k v]
     (let [query (if (map? v) 
                   v
                   {:query v
                    :parser driver/get-text})
           results (parse-item searcher query)
           result (if (and (vector? results) (= 1 (count results)))
                    (first results)
                    results)]
       (if (and (vector? result) (empty? result))
         acc
         (assoc acc k result))))
   {}
   queries))

(defmethod parse-map #{:query :parser} [searcher {query :query parser :parser}]
  (mapv
   #(parser (driver/get-element %))
   (parse-item searcher query)))

(defmethod parse-map #{:query :child} [searcher {query :query child :child}]
  (mapv #(parse-page % child) (parse-item searcher query)))

(defmethod parse-map #{:query :child :parser} [searcher {query :query child :child parser :parser}]
  (mapv #(parser (parse-page % child)) (parse-item searcher query)))

(defmethod parse-item clojure.lang.PersistentArrayMap [searcher item]
  (parse-map searcher item))

(defmethod parse-item clojure.lang.IFn [searcher parser]
  (parser (driver/get-element searcher)))

(defmethod parse-item clojure.lang.PersistentVector [searcher item]
  (reduce #(into %1 (driver/find-elements searcher %2)) [] item))

(defmethod parse-item :default [searcher item]
  (try
    [(driver/find-element searcher item)]
    (catch NoSuchElementException _ [])))

(defn get-page [^String url]
  (driver/set-page url)
  (parse-page (driver/get-document) constants/queries))

(defn get-links-from-page [page]
  (reduce
   (fn [acc k]
     (into acc (mapv :link (get page k))))
   (sorted-set)
   constants/link-lists))

(defn scrape-all-recipes []
  (get-page (:home constants/urls)))