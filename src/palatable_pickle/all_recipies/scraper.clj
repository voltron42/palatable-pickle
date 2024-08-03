(ns palatable-pickle.all-recipies.scraper 
  (:require [clojure.pprint :as pprint]
            [palatable-pickle.all-recipies.constants :as constants]
            [palatable-pickle.driver :as driver]))

(defmulti parse-item type)

(defmethod parse-item clojure.lang.PersistentArrayMap [item]
  (type item))

(defmethod parse-item clojure.lang.PersistentVector [item]
  (type item))

(defmethod parse-item :default [item]
  (driver/find-element item))

(defn parse-page [queries]
  (reduce-kv
   (fn [acc k v]
     (assoc acc k (parse-item v)))
   {}
   queries))

(defn get-page [url]
  (driver/set-page (driver/get-driver) url)
  (parse-page constants/queries))

(defn scrape-all-recipes []
  (let [page (get-page (:home constants/urls))
        body (parse-page constants/queries)
        types (set (vals body))]
    (pprint/pprint types)))