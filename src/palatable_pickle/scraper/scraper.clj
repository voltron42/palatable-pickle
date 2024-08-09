(ns palatable-pickle.scraper.scraper 
  (:require [clojure.pprint :as pp]
            [palatable-pickle.all-recipes.constants :as constants]
            [palatable-pickle.driver :as driver]) 
  (:import [org.openqa.selenium NoSuchElementException]))

(defn wrap-element [searcher]
  (let [element (driver/get-element searcher)]
    (reify constants/Node
      (get-text [_]
        (driver/get-text element))
      (get-attribute [_ attr-name]
        (driver/get-attribute element attr-name)))))

(defmulti parse-map #(set (keys %2)))

(defmulti parse-item #(type %2))

(defn parse-page [searcher queries]
  (reduce-kv
   (fn [acc k v]
     (let [query (if (or (fn? v) (map? v))
                   v
                   {:query v
                    :parser constants/get-text})
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
   #(parser (wrap-element %))
   (parse-item searcher query)))

(defmethod parse-map #{:child} [searcher {child :child}]
  (parse-page searcher child))

(defmethod parse-map #{:query :child} [searcher {query :query child :child}]
  (mapv #(parse-page % child) (parse-item searcher query)))

(defmethod parse-map #{:query :child :parser} [searcher {query :query child :child parser :parser}]
  (mapv #(parser (parse-page % child)) (parse-item searcher query)))

(defmethod parse-item clojure.lang.PersistentArrayMap [searcher item]
  (parse-map searcher item))

(defmethod parse-item clojure.lang.IFn [searcher parser]
  (parser (wrap-element searcher)))

(defmethod parse-item clojure.lang.PersistentVector [searcher item]
  (reduce #(into %1 (driver/find-elements searcher %2)) [] item))

(defmethod parse-item :default [searcher item]
  (try
    [(driver/find-element searcher item)]
    (catch NoSuchElementException _ [])))

(defn get-page [^String url]
  (pp/pprint {:url url})
  (driver/using-browser
   (fn [browser]
     (driver/set-page browser url)
     (Thread/sleep 500)
     (let [page (parse-page (driver/get-document browser) constants/queries)]
       (assoc page :url url)))))

(defn get-links-from-page [page]
  (reduce
   (fn [acc k]
     (let [link-list (get page k)
           link-list (if (map? link-list) [link-list] link-list)]
       (into acc (mapv :link link-list))))
   (sorted-set)
   constants/link-lists))