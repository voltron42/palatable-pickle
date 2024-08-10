(ns palatable-pickle.all-recipes.constants 
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [palatable-pickle.util.query :as query])
  (:import [java.io ByteArrayInputStream])
  (:gen-class))

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
  {:home "https://www.allrecipes.com/"})

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

(def link-lists #{:menu :breadcrumb :list-page-item :listed-card :view-recipe-button})

(defprotocol Node
  (get-text [this])
  (get-attribute [this attr-name]))

(defn get-link [elem]
  {:label (get-text elem)
   :link (get-attribute elem "href")})

(defn parse-number [elem]
  (edn/read-string (get-text elem)))

(defn- parse-image-from-noscript [noscript-img-xml]
  (-> noscript-img-xml
      (get-text)
      (.getBytes)
      (ByteArrayInputStream.)
      (xml/parse)
      (zip/xml-zip)
      (first)
      (:attrs)
      (:src)))

(def card {:title (query/->Query :class-name "card__title-text")
           :img-src {:query (query/->Query :xpath "//div[contains(@class,'img-placeholder')]/noscript")
                     :parser parse-image-from-noscript}
           :link #(get-attribute % "href")})

(def listed-card {:query [(query/->Query :xpath "//a[contains(@class,'mntl-card-list-items')]")]
                  :child card})

(comment
  #(let [text (get-text %)]
     (str/trim (subs text (+ (str/index-of text "</span>") (count "</span>"))))))

(def queries
  {:article-title (query/->Query :tag-name "h1")
   :menu {:query [(query/->Query :xpath "//li[contains(@class,'mntl-fullscreen-nav__sublist-item')]/a")]
          :parser get-link}
   :breadcrumb {:query [(query/->Query :xpath "//li[contains(@class,'mntl-breadcrumbs__item')]/a")]
                :child {:label (query/->Query :class-name "link__wrapper")
                        :link #(get-attribute % "href")}}
   :view-recipe-button {:query (query/->Query :xpath "//a[span[contains(text(),'View Recipe')]]")
                        :parser get-link}
   :recipe-details {:query [(query/->Query :class-name "mm-recipes-details__item")]
                    :child {:label (query/->Query :class-name "mm-recipes-details__label")
                            :value (query/->Query :class-name "mm-recipes-details__value")}}
   :ingredient-item {:query [(query/->Query :class-name "mm-recipes-structured-ingredients__list-item")]
                     :child {:quantity (query/->Query :xpath ".//span[@data-ingredient-quantity]")
                             :unit (query/->Query :xpath ".//span[@data-ingredient-unit]")
                             :name (query/->Query :xpath ".//span[@data-ingredient-name]")}}
   :gallery {:query [(query/->Query :xpath "//figure[contains(@class,'mntl-universal-image')]/div/img")]
             :child {:img-src #(get-attribute % "data-src")}}
   :related-pages {:query (query/->Query :class-name "mntl-recirc-section__content")
                                     :child {:listed-card listed-card}}
   :recipe {:child {:recipe-details {:query [(query/->Query :class-name "mm-recipes-details__item")]
                                     :child {:label (query/->Query :class-name "mm-recipes-details__label")
                                             :value (query/->Query :class-name "mm-recipes-details__value")}}
                    :ingredient-item {:query [(query/->Query :class-name "mm-recipes-structured-ingredients__list-item")]
                                      :child {:quantity (query/->Query :xpath ".//span[@data-ingredient-quantity]")
                                              :unit (query/->Query :xpath ".//span[@data-ingredient-unit]")
                                              :name (query/->Query :xpath ".//span[@data-ingredient-name]")}}
                    :recipe-steps {:query [(query/->Query :xpath "//div[contains(@class,'mm-recipes-steps')]/ol/li")]
                                   :child {:text (query/->Query :xpath "p")
                                           :img-src {:query (query/->Query :tag-name "img")
                                                     :parser #(get-attribute % "data-src")}}}
                    :servings {:query (query/->Query :xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__servings')]/th/span[2]")
                               :parser parse-number}
                    :calories {:query (query/->Query :xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__calories')]/th/span[2]")
                               :parser parse-number}
                    :nutritional-facts {:query [(query/->Query :xpath "//tbody[contains(@class,'mm-recipes-nutrition-facts-label__table-body')]/tr[position() > 1]")]
                                        :child {:name (query/->Query :xpath "td[1]/span")
                                                :quantity (query/->Query :xpath "td[1]/text()[last()]")
                                                :percent (query/->Query :xpath "td[2]")}}}}
   :recipe-list {:child {:list-page-item {:query [(query/->Query :xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")]
                                          :parser get-link}
                         :five-post {:query (query/->Query :class-name "mntl-vertical-list__wrapper")
                                     :child {:featured {:query [(query/->Query :xpath "//a[contains(@class,'mntl-five-post__featured')]")]
                                                        :child card}
                                             :listed-card listed-card}}
                         :vertical-card-list {:query (query/->Query :class-name "mntl-vertical-list__wrapper")
                                              :child {:listed-card listed-card}}
                         :spotlight {:query (query/->Query :class-name "mntl-document-spotlight")
                                     :child {:listed-card listed-card}}
                         :full-list {:query (query/->Query :class-name "mntl-taxonomysc-article-list-group")
                                     :child {:listed-card listed-card}}}}})
   