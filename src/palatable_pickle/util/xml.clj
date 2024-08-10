(ns palatable-pickle.util.xml 
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]))

(defn zip-str [s]
  (zip/xml-zip
   (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(defn node->string [node]
  (with-out-str (xml/emit-element (zip/root [node]))))

(defn get-child [node tag]
  (first (filter #(= tag (:tag %)) (:content node))))

(defn exclude [nodes [tag-names]]
  (reduce (fn [nodes tag] (filter #(not= tag (:tag %)) nodes)) nodes tag-names))