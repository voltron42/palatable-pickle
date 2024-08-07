(ns palatable-pickle.all-recipes.constants 
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [palatable-pickle.driver :as driver])
  (:import [java.io ByteArrayInputStream]
           [org.openqa.selenium By]))

(def fractions {"⅛" 1/8
                "¼" 1/4
                "⅓" 1/3
                "⅜" 3/8
                "½" 1/2
                "⅝" 5/8
                "⅔" 2/3
                "¾" 3/4
                "⅞" 7/8
                })

(def urls 
  {:home "https://www.allrecipes.com/"
   :recipe-pattern "https://www.allrecipes.com/recipe/{id-int}/{id-keyword}/"
   :old-pattern "https://www.allrecipes.com/{old-id-keyword}/"
   :list-pattern "https://www.allrecipes.com/recipes/{id-int}/{list-path-step}/.."})

(defmulti get-page-identifiers-by-type first)

(defmulti get-full-id-from-page-identifiers :page-type)

(defmethod get-page-identifiers-by-type nil [_] {:page-type :home})

(defmethod get-full-id-from-page-identifiers :home [_] "home")

(defmethod get-page-identifiers-by-type :recipe 
  [[page-type recipe-id recipe-key]] 
  {:page-type page-type 
   :recipe-id recipe-id 
   :recipe-key recipe-key})

(defmethod get-full-id-from-page-identifiers :recipe 
  [{:keys [page-type recipe-id recipe-key]}] 
  (str/join "_" [(name page-type) recipe-id (name recipe-key)]))

(defmethod get-page-identifiers-by-type :recipes
  [[_ recipe-list-id & recipe-list-path]] 
  {:page-type :recipe-list 
   :recipe-list-id recipe-list-id 
   :recipe-list-path (vec recipe-list-path)})

(defmethod get-full-id-from-page-identifiers :recipe-list
  [{:keys [recipe-list-id recipe-list-path]}] 
  (str/join "_" ["recipe-list" recipe-list-id (name (last recipe-list-path))]))

(defmethod get-page-identifiers-by-type :article
  [[page-type & id-path]] 
  {:page-type page-type 
   :id-path id-path})

(defmethod get-full-id-from-page-identifiers :article 
  [{:keys [page-type id-path]}] 
  (str/join "_" (into [(name page-type)] (mapv name id-path))))

(defmethod get-page-identifiers-by-type :food-news-trends
  [[page-type & id-path]] 
  {:page-type page-type 
   :id-path id-path})

(defmethod get-full-id-from-page-identifiers :food-news-trends 
  [{:keys [page-type id-path]}] 
  (str/join "_" (into [(name page-type)] (mapv name id-path))))

(defmethod get-page-identifiers-by-type :kitchen-tips
  [[page-type & id-path]] 
  {:page-type page-type 
   :id-path id-path})

(defmethod get-full-id-from-page-identifiers :kitchen-tips 
  [{:keys [page-type id-path]}] 
  (str/join "_" (into [(name page-type)] (mapv name id-path))))

(defmethod get-page-identifiers-by-type :gallery
  [[page-type gallery-key]] 
  {:page-type page-type 
   :gallery-key gallery-key})

(defmethod get-full-id-from-page-identifiers :gallery
  [{:keys [page-type gallery-key]}] 
  (str/join "_" (mapv name [page-type gallery-key])))

(defmethod get-page-identifiers-by-type :default
  [[page-key]] 
  {:page-type :other 
   :page-key page-key})

(defmethod get-full-id-from-page-identifiers :other
  [{:keys [page-type page-key]}] 
  (str/join "_" (mapv name [page-type page-key])))

(defn get-page-identifiers [url]
  (let [path (->> (str/split url #"/")
                  (drop 3)
                  (mapv #(let [step (try
                                      (edn/read-string %)
                                      (catch Throwable _
                                        (symbol %)))]
                           (if (symbol? step)
                             (keyword (name step))
                             step))))]
    (get-page-identifiers-by-type path)))

(defn get-link [elem]
  {:label (driver/get-text elem)
   :link (driver/get-attribute elem "href")})

(defn parse-number [elem]
  (edn/read-string (driver/get-text elem)))

(def link-lists #{:menu :breadcrumb :list-page-item :listed-card :view-recipe-button})

(defn- parse-image-from-noscript [noscript-img-xml]
  (-> noscript-img-xml
      (driver/get-text)
      (.getBytes)
      (ByteArrayInputStream.)
      (xml/parse)
      (zip/xml-zip)
      (first)
      (:attrs)
      (:src)))

(def queries
  {:article-title (By/tagName "h1")
   :menu {:query [(By/xpath "//li[contains(@class,'mntl-fullscreen-nav__sublist-item')]/a")]
          :parser get-link}
   :breadcrumb {:query [(By/xpath "//li[contains(@class,'mntl-breadcrumbs__item')]/a")]
                :child {:label (By/className "link__wrapper")
                        :link #(driver/get-attribute % "href")}}
   :list-page-item {:query [(By/xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")]
                    :parser get-link}
   :listed-card {:query [(By/xpath "//a[contains(@class,'mntl-card-list-items')]")]
                 :child {:title (By/className "card__title-text")
                         :img-src {:query (By/xpath "//div[contains(@class,'img-placeholder')]/noscript")
                                   :parser parse-image-from-noscript}
                         :link #(driver/get-attribute % "href")}}
   :view-recipe-button {:query (By/xpath "//a[span[contains(text(),'View Recipe')]]")
                        :parser get-link}
   :recipe-details {:query [(By/className "mm-recipes-details__item")]
                    :child {:label (By/className "mm-recipes-details__label")
                            :value (By/className "mm-recipes-details__value")}}
   :ingredient-item {:query [(By/className "mm-recipes-structured-ingredients__list-item")]
                     :child {:quantity (By/xpath ".//span[@data-ingredient-quantity]")
                             :unit (By/xpath ".//span[@data-ingredient-unit]")
                             :name (By/xpath ".//span[@data-ingredient-name]")}}
   :gallery {:query [(By/xpath "//figure[contains(@class,'mntl-universal-image')]/div/img")]
             :child {:img-src #(driver/get-attribute % "data-src")}}
   :recipe-steps {:query [(By/xpath "//div[contains(@class,'mm-recipes-steps')]/ol/li")]
                  :child {:text (By/xpath "p")
                          :img-src {:query (By/tagName "img")
                                    :parser #(driver/get-attribute % "data-src")}}}
   :servings {:query (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__servings')]/th/span[2]")
              :parser parse-number}
   :calories {:query (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__calories')]/th/span[2]")
              :parser parse-number}
   :nutritional-facts {:query [(By/xpath "//tbody[contains(@class,'mm-recipes-nutrition-facts-label__table-body')]/tr[position() > 1]")]
                       :child {:name (By/xpath "td[1]/span")
                               :quantity {:query (By/xpath "td[1]")
                                          :parser #(let [text (driver/get-text %)]
                                                     (str/trim (subs text (+ (str/index-of text "</span>") (count "</span>")))))}
                               :percent (By/xpath "td[2]")}}})
   