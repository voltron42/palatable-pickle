(ns palatable-pickle.driver 
  (:import (org.openqa.selenium.chrome
            ChromeDriver
            ChromeOptions
            ChromeDriverService$Builder
            ChromeDriverService)
           (org.openqa.selenium.remote CapabilityType)))

(defn build-driver [] 
  (let [^ChromeDriverService service (-> (ChromeDriverService$Builder.)
                                         (.usingPort 4444)
                                         (.build))
        ^ChromeOptions chrome-opts (doto (ChromeOptions.) 
                                     (.addArguments ["--no-sandbox"])
                                     (.setCapability CapabilityType/ACCEPT_INSECURE_CERTS true)
                                     )]
    {:driver (ChromeDriver. service chrome-opts)}))
