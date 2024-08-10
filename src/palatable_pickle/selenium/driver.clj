(ns palatable-pickle.selenium.driver 
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]) 
  (:import (org.openqa.selenium.chrome
            ChromeDriver
            ChromeDriverService
            ChromeDriverService$Builder
            ChromeOptions)
           (org.openqa.selenium.remote CapabilityType)
           org.openqa.selenium.WebElement))

(defn- build-driver [port-number] 
  (let [^ChromeDriverService service (-> (ChromeDriverService$Builder.)
                                         (.usingPort port-number)
                                         (.build))
        ^ChromeOptions chrome-opts (doto (ChromeOptions.)
                                     (.addArguments ["--no-sandbox" "--headless"])
                                     (.setCapability CapabilityType/ACCEPT_INSECURE_CERTS true))]
    (ChromeDriver. service chrome-opts)))

(defn- wrap-retries [func]
  (loop [retries 5]
    (let [{result :result exception :exception}
          (try
            {:result (func)}
            (catch Throwable e {:exception e}))]
      (if (nil? exception)
        result
        (if (zero? retries)
          (throw exception)
          (recur (dec retries)))))))

(defprotocol Element
  (get-attribute [this ^String name])
  (get-text [this]))

(defn- wrap-element [^WebElement element]
  (reify Element
    (get-attribute [_ name]
      (wrap-retries #(.getAttribute element name)))
    (get-text [_]
      (str/trim (wrap-retries #(.getAttribute element "innerHTML"))))))

(defprotocol Searcher
  (find-element [this by])
  (find-elements [this by])
  (get-element [this]))

(defn- wrap-element-as-searcher [^WebElement element]
  (reify Searcher
    (find-element [_ by]
      (wrap-element-as-searcher (wrap-retries #(.findElement element by))))
    (find-elements [_ by]
      (mapv wrap-element-as-searcher (wrap-retries #(.findElements element by))))
    (get-element [_]
      (wrap-element element))))

(defprotocol Browser
  (set-page [this url])
  (get-url [this])
  (get-title [this])
  (get-document [this]))

(defn using-browser [func]
  (let [driver (build-driver 4445)
        browser (reify Browser
                  (set-page [_ url]
                    (wrap-retries #(.get driver url)))
                  (get-url [_]
                    (wrap-retries #(.getCurrentUrl driver)))
                  (get-title [_]
                    (wrap-retries #(.getTitle driver)))
                  (get-document [_]
                    (reify Searcher
                      (find-element [_ by]
                        (wrap-element-as-searcher (wrap-retries #(.findElement driver by))))
                      (find-elements [_ by]
                        (mapv wrap-element-as-searcher (wrap-retries #(.findElements driver by))))
                      (get-element [_] nil))))]
    (try
      (func browser)
      (finally
        (wrap-retries
         #(try
            (.close driver)
            (catch Throwable t
              (pp/pprint t))
            (finally
              (try
                (.quit driver)
                (catch Throwable t
                  (pp/pprint t))))))))))
