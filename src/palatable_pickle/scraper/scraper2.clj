(ns palatable-pickle.scraper.scraper2 
  (:require [clj-http.client :as client]
            [clj-xpath.core :as xpath] 
            [clojure.string :as str]
            [palatable-pickle.all-recipes.constants :as constants]
            [palatable-pickle.util.xml :as xml]))

(defmulti query->xpath #(.query-type %))

(defmethod query->xpath :xpath [query]
  (.query query))

(defmethod query->xpath :class-name [query]
  (str "//*[contains(@class,'" (.query query) "')]"))

(defmethod query->xpath :tag-name [query]
  (str "//" (.query query)))

(defn- find-elements [html query]
  (xpath/$x (str html) (query->xpath query)))

(defn- find-element [html query]
  (first (xpath/$x (str html) (query->xpath query))))

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

(defmethod parse-map #{:query :parser} [html {query :query parser :parser}]
  (mapv
   #(parser (wrap-element %))
   (parse-item html query)))

(defmethod parse-map #{:child} [html {child :child}]
  (parse-page html child))

(defmethod parse-map #{:query :child} [html {query :query child :child}]
  (mapv #(parse-page % child) (parse-item html query)))

(defmethod parse-map #{:query :child :parser} [html {query :query child :child parser :parser}]
  (mapv #(parser (parse-page % child)) (parse-item html query)))

(defmethod parse-item clojure.lang.PersistentArrayMap [html item]
  (parse-map html item))

(defmethod parse-item clojure.lang.IFn [html parser]
  (parser (wrap-element html)))

(defmethod parse-item clojure.lang.PersistentVector [html item]
  (reduce #(into %1 (find-elements html %2)) [] item))

(defmethod parse-item :default [html item]
  (if-let [element (find-element html item)]
    [element]
    []))

(defn get-page [url]
  (let [html (:body (client/get url {:accept :html}))
        html (str/replace html "<!DOCTYPE html>\n" "")
        html (str/replace html "&" "&amp;")
        doc (xml/zip-str html)
        body (xml/get-child doc :body)
        body (xml/exclude body :script)
        xml (xml/node->string body)
        _ (spit "resources/raw/body.xml" xml)
        doc (xpath/xml->doc xml)
        page (parse-page doc constants/queries)]
    (assoc page :url url)))