(ns palatable-pickle.all-recipies.constants 
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [palatable-pickle.driver :as driver]) 
  (:import [org.openqa.selenium By]))

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
                :child {:label (By/className "link__wrapper")
                        :link #(driver/get-attribute % "href")}}
   :list-page-item {:query [(By/xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")]
                    :parser get-link}
   :listed-card {:query [(By/xpath "//a[contains(@class,'mntl-card-list-items')]")]
                 :child {:title (By/className "card__title-text")
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
   :recipe-steps [(By/xpath "//div[contains(@class,'mm-recipes-steps')]/ol/li/p")]
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
   