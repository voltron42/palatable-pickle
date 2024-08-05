(defproject palatable-pickle "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.4"]
                 [org.seleniumhq.selenium/selenium-java "4.23.0"]
                 [org.seleniumhq.selenium/selenium-chrome-driver "4.23.0"]
                 [prismatic/schema "1.4.1"]]
  :main ^:skip-aot palatable-pickle.core
  :target-path "target/%s"
  
  :profiles {:user {:plugins [[lein-environ "1.2.0"]
                              [lein-ancient "1.0.0-RC3"]]}
             :test  {:dependencies [[criterium "0.4.6"]]
                     :plugins [[lein-junit "1.1.8"]
                               [lein-test-report-junit-xml "0.2.0"]]
                     :test-report-junit-xml {:output-dir "test-results"}}})
