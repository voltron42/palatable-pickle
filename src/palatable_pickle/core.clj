(ns palatable-pickle.core
  (:gen-class) 
  (:require [palatable-pickle.all-recipies.scraper :as scraper]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (scraper/scrape-all-recipes))
