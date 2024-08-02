(ns palatable-pickle.driver 
  (:import (org.openqa.selenium.chrome
            ChromeDriver
            ChromeOptions
            ChromeDriverService$Builder
            ChromeDriverService)
           (org.openqa.selenium.remote CapabilityType)))

(defn build-driver [port-number] 
  (let [^ChromeDriverService service (-> (ChromeDriverService$Builder.)
                                         (.usingPort port-number)
                                         (.build))
        ^ChromeOptions chrome-opts (doto (ChromeOptions.)
                                     (.addArguments ["--no-sandbox" "--headless"])
                                     (.setCapability CapabilityType/ACCEPT_INSECURE_CERTS true))]
    (ChromeDriver. service chrome-opts)))
