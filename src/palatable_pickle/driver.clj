(ns palatable-pickle.driver 
  (:import (org.openqa.selenium.chrome
            ChromeDriver
            ChromeDriverService
            ChromeDriverService$Builder
            ChromeOptions)
           (org.openqa.selenium.remote CapabilityType)
           org.openqa.selenium.WebElement) 
  (:require [clojure.string :as str]))

(defonce ^:private driver (atom nil))

(defn- build-driver [port-number] 
  (let [^ChromeDriverService service (-> (ChromeDriverService$Builder.)
                                         (.usingPort port-number)
                                         (.build))
        ^ChromeOptions chrome-opts (doto (ChromeOptions.)
                                     (.addArguments ["--no-sandbox" "--headless"])
                                     (.setCapability CapabilityType/ACCEPT_INSECURE_CERTS true))]
    (ChromeDriver. service chrome-opts)))

(defn- get-driver []
  (when (nil? @driver)
    (reset! driver (build-driver 4445)))
  @driver)

(defn close-driver []
  (doto (get-driver)
    (.close)
    (.quit))
  (reset! driver nil))



(defn set-page [^String url]
  (-> (get-driver) (.get url)))

(defn get-url []
  (-> (get-driver) (.getCurrentUrl)))

(defn get-title []
  (-> (get-driver) (.getTitle)))

(defprotocol Button
  (click [this]))

(defprotocol Element
  (get-attribute [this ^String name])
  (get-text [this]))

(defn wrap-element [^WebElement element]
  (reify Element
    (get-attribute [_ name]
      (-> element
          (.getAttribute name)))
    (get-text [_]
      (str/trim (.getAttribute element "innerHTML")))))

(defprotocol Searcher
  (find-element [this by])
  (find-elements [this by])
  (get-element [this]))

(defn wrap-element-as-searcher [^WebElement element]
  (reify Searcher
    (find-element [_ by]
      (-> element
          (.findElement by)
          (wrap-element-as-searcher)))
    (find-elements [_ by]
      (->> (.findElements element by)
           (mapv wrap-element-as-searcher)))
    (get-element [_]
      (wrap-element element))))

(defn get-document []
  (reify Searcher
    (find-element [_ by]
      (-> (get-driver)
          (.findElement by)
          (wrap-element-as-searcher)))
    (find-elements [_ by]
      (->> (.findElements (get-driver) by)
           (mapv wrap-element-as-searcher)))
    (get-element [_] nil)))
