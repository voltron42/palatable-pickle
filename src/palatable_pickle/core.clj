(ns palatable-pickle.core
  (:gen-class) 
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [palatable-pickle.scraper.scraper :as scraper]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clj-time.core :as t]
            [palatable-pickle.all-recipes.constants :as constants]))

(defn read-and-publish-single-page [url]
  (let [page (scraper/get-page url)
        page (merge page (constants/get-page-identifiers (:url page)))]
    (spit 
     (io/file 
      "resources"
      "all-recipes"
      (str (constants/get-full-id-from-page-identifiers page) ".edn")) 
     (with-out-str (pp/pprint page)))))

(defn read-links-from-existing-files []
  (let [files (vec (.listFiles (io/file "resources" "all-recipes")))
        {:keys [urls links]}
        (reduce
         (fn [{:keys [urls links]} file-name]
           (let [page (edn/read-string (slurp file-name))
                 page-links (scraper/get-links-from-page page)]
             {:urls (into urls #{(:url page)})
              :links (into links page-links)}))
         {:urls #{}
          :links #{}}
         files)] 
    (if (empty? files)
      #{(:home constants/urls)}
      (set/difference links urls))))

(def default-minutes 1)

(defn -main [& [minute-str]]
   (let [start-time (t/now)
         minutes (if (and (int? minute-str) (pos? minute-str))
                      minute-str
                      (if (string? minute-str)
                        (let [temp (edn/read-string minute-str)]
                          (if (and (int? temp) (pos? temp))
                            temp
                            (throw (IllegalArgumentException. (str "Argument must be a positive integer. \"" minute-str "\" is not")))))
                        default-minutes))
         end-time (t/plus start-time (t/minutes minutes))
         all-links (read-links-from-existing-files)]
     (pp/pprint {:total-count (count all-links)})
     (loop [links all-links
            counted 1]
       (when (and (not-empty links) (t/after? end-time (t/now)))
         (read-and-publish-single-page (first links))
         (pp/pprint {:counted counted
                     :remaining (- (count all-links) counted)
                     :elapsed (t/in-minutes (t/interval start-time (t/now)))
                     :full-time minutes})
         (recur (rest links) (inc counted))))))
