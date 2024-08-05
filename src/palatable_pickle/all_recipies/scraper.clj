(ns palatable-pickle.all-recipies.scraper 
  (:require [clojure.set :as set]
            [palatable-pickle.all-recipies.constants :as constants]
            [palatable-pickle.driver :as driver]) 
  (:import [org.openqa.selenium NoSuchElementException]))

(defmulti parse-map #(set (keys %2)))

(defmulti parse-item #(type %2))

(defn parse-page [searcher queries]
  (reduce-kv
   (fn [acc k v]
     (let [query (if (or (fn? v) (map? v))
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
  (println url)
  (driver/using-browser
   (fn [browser]
     (driver/set-page browser url)
     (Thread/sleep 500)
     (let [page (parse-page (driver/get-document browser) constants/queries)]
       (assoc page :url url)))))

(defn get-links-from-page [page]
  (reduce
   (fn [acc k]
     (into acc (mapv :link (get page k))))
   (sorted-set)
   constants/link-lists))

(defn get-pages [urls]
  (let [pages (mapv get-page urls)]
    {:pages pages
     :links (reduce #(into %1 (get-links-from-page %2)) (sorted-set) pages)}))

(defn scrape-all-recipes []
  (let [url (:home constants/urls)
        home-page (get-page url)
        home-page-links (get-links-from-page home-page)
        {pages-1 :pages links-1 :links} (get-pages home-page-links)
        visited-1 (set/union #{url} home-page-links)
        target-links-1 (set/difference links-1 visited-1)
        {pages-2 :pages links-2 :links} (get-pages target-links-1)
        visited-2 (set/union visited-1 target-links-1)
        target-links-2 (set/difference links-2 visited-2)
        all-pages (into [home-page] pages-1 pages-2)]
    {:next-links target-links-2
     :pages all-pages}))