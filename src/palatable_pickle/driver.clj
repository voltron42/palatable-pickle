(ns palatable-pickle.driver 
  (:import org.openqa.selenium.By
           (org.openqa.selenium.chrome
            ChromeDriver
            ChromeDriverService
            ChromeDriverService$Builder
            ChromeOptions)
           (org.openqa.selenium.remote CapabilityType)
           org.openqa.selenium.WebElement))

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
    (reset! driver (build-driver 4444)))
  driver)

(defn set-page [^String url]
  (-> (get-driver) (.get url)))

(defn get-url []
  (-> (get-driver) (.getCurrentUrl)))

(defn get-title []
  (-> (get-driver) (.getTitle)))

(defn find-element [^By by]
  (-> (get-driver) (.findElement by)))

(defn find-elements [^By by]
  (mapv identity (-> (get-driver) (.findElements by))))

(defn find-child [^WebElement element ^By by]
  (-> element (.findElement by)))

(defn find-children [^WebElement element ^By by]
  (mapv identity (-> element (.findElements by))))
