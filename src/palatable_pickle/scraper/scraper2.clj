(ns palatable-pickle.scraper.scraper2 
  (:require [clj-http.client :as client]
            [palatable-pickle.all-recipes.constants :as constants]
            [clj-xpath.core :as xpath])
  (:import [org.seleniumhq.selenium By$ByXPath By$ByClassName By$ByTagName]))

(defmulti by->xpath type)

(comment
  {:className
   {:by
    #object[org.openqa.selenium.By$ByClassName 0x5a515e5d "By.className: show"],
    :toString "By.className: show"},
   :tagName
   {:by
    #object[org.openqa.selenium.By$ByTagName 0x5cd96b41 "By.tagName: p"],
    :toString "By.tagName: p"},
   :xpath
   {:by
    #object[org.openqa.selenium.By$ByXPath 0x3d620a1 "By.xpath: //button[contains(text(),'Submit')]"],
    :toString "By.xpath: //button[contains(text(),'Submit')]"}})

(defmethod by->xpath By$ByXPath [by])

(defmethod by->xpath By$ByClassName [by])

(defmethod by->xpath By$ByTagName [by])

(defn- find-elements [html by])

(defn- find-element [html by])

(defn wrap-element [html]
  (let [node (xpath/$x "." html)]
    (reify constants/Node
      (get-text [_]
        (:text node))
      (get-attribute [_ attr]
        (-> node :attrs (keyword attr))))))

(defmulti parse-map #(set (keys %2)))

(defmulti parse-item #(type %2))

(defn- parse-page [html queries]
    (reduce-kv
   (fn [acc k v]
     (let [query (if (or (fn? v) (map? v))
                   v
                   {:query v
                    :parser constants/get-text})
           results (parse-item html query)
           result (if (and (vector? results) (= 1 (count results)))
                    (first results)
                    results)]
       (if (and (vector? result) (empty? result))
         acc
         (assoc acc k result))))
   {}
   queries))

(defmethod parse-item clojure.lang.PersistentArrayMap [searcher item]
  (parse-map searcher item))

(defmethod parse-item clojure.lang.IFn [searcher parser]
  (parser (wrap-element searcher)))

(defmethod parse-item clojure.lang.PersistentVector [searcher item]
  (reduce #(into %1 (find-elements searcher %2)) [] item))

(defmethod parse-item :default [searcher item]
  [(find-element searcher item)])


(defn get-page [url]
  (let [html (:body (client/get url {:accept :html}))
        page (parse-page html constants/queries)]
    (assoc page :url url)))