(ns palatable-pickle.all-recipies.constants 
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [palatable-pickle.driver :as driver]) 
  (:import [org.openqa.selenium By]))

(def urls 
  {:home "https://www.allrecipes.com/"
   :recipe-pattern "https://www.allrecipes.com/recipe/{id-int}/{id-keyword}/"
   :old-pattern "https://www.allrecipes.com/{old-id-keyword}/"
   :list-pattern "https://www.allrecipes.com/recipes/{id-int}/{list-path-step}/.."})

(defn get-link [elem]
  {:label (driver/get-text elem)
   :link (driver/get-attribute elem "href")})

(defn parse-number [elem]
  (edn/read-string (driver/get-text elem)))

(def link-lists #{:menu :breadcrumb :list-page-item :listed-card :view-recipe-button})

(def queries
  {:article-title (By/tagName "h1")
   :menu {:query [(By/xpath "//li[contains(@class,'mntl-fullscreen-nav__sublist-item')]/a")]
          :parser get-link}
   :breadcrumb {:query [(By/xpath "//li[contains(@class,'mntl-breadcrumbs__item')]/a")]
                :parser get-link}
   :list-page-item {:query [(By/xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")]
                    :parser get-link}
   :listed-card {:query [(By/xpath "//a[contains(@class,'mntl-card-list-items')]")]
                       :child {:title (By/xpath "./span[contains(@class,'card__title-text')]")
                               :link #(driver/get-attribute % "href")}}
   :view-recipe-button {:query (By/xpath "//a[span[contains(text(),'View Recipe')]]")
                        :parser get-link}
   :recipe-details {:query [(By/className "mm-recipes-details__item")]
                    :child {:label (By/className "mm-recipes-details__label")
                            :value (By/className "mm-recipes-details__value")}}
   :ingredient-item {:query [(By/className "mm-recipes-structured-ingredients__list-item")]
                     :child {:ammount (By/xpath "./.[1]")
                             :unit (By/xpath "./.[2]")
                             :ingredient (By/xpath "./.[3]")}}
   :recipe-steps [(By/xpath "//div[contains(@class,'mm-recipes-steps')]/ol/li/p")]
   :servings {:query (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__servings')]/th/span[2]")
              :parser parse-number}
   :calories {:query (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__calories')]/th/span[2]")
              :parser parse-number}
   :nutritional-facts {:query (By/xpath "//tbody[contains(@class,'')]/tr[position() > 1]")
                       :child {:full-label (By/xpath "./td[1]")
                               :name (By/xpath "./td[1]/span")
                               :percent (By/xpath "./td[2]")}
                       :parser (fn [{full-label :full-label name :name percent :percent}]
                                 (let [value (-> full-label (str/replace name "") (str/trim))]
                                   {:name name
                                    :value value
                                    :percent percent}))}})
   