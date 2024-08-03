(ns palatable-pickle.all-recipies.constants 
  (:require [clojure.edn :as edn]) 
  (:import [org.openqa.selenium By]))

(def urls 
  {:home "https://www.allrecipes.com/"
   :recipe-pattern "https://www.allrecipes.com/recipe/{id-int}/{id-keyword}/"
   :old-pattern "https://www.allrecipes.com/{old-id-keyword}/"
   :list-pattern "https://www.allrecipes.com/recipes/{id-int}/{list-path-step}/.."})

(def queries
  {:article-title (By/tagName "h1")
   :menu [(By/xpath "//li[contains(@class,'mntl-fullscreen-nav__sublist-item')]/a")]
   :breadcrumb [(By/xpath "//li[contains(@class,'mntl-breadcrumbs__item')]/a")]
   :list-page-item [(By/xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")]
   :listed-card-xpath
   {:link
    {:item [(By/xpath "//a[contains(@class,'mntl-card-list-items')]")]
     :child
     {:title (By/xpath "./span[contains(@class,'card__title-text')]")}}}
   :view-recipe-button-xpath (By/xpath "//a[span[contains(text(),'View Recipe')]]")
   :recipe
   {:details
    {:item [(By/className "mm-recipes-details__item")]
     :child
     {:label (By/className "mm-recipes-details__label")
      :value (By/className "mm-recipes-details__value")}}
    :ingredient-item
    {:item [(By/xpath "//.[contains(@class,'mm-recipes-structured-ingredients__list-item')]")]
     :child
     {:ammount (By/xpath "./.[1]")
      :unit (By/xpath "./.[2]")
      :ingredient (By/xpath "./.[3]")}}
    :recipe-steps [(By/xpath "//div[contains(@class,'mm-recipes-steps')]/ol/li/p")]
    :servings 
    {:item (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__servings')]/th/span[2]")
     :parser edn/read-string}
    :calories 
    {:item (By/xpath "//tr[contains(@class,'mm-recipes-nutrition-facts-label__calories')]/th/span[2]")
     :parser edn/read-string}
    :nutritional-facts
    {:item (By/xpath "//tbody[contains(@class,'')]/tr[position() > 1]")
     :child
     {:full-label (By/xpath "./td[1]")
      :name (By/xpath "./td[1]/span")
      :percent (By/xpath "./td[2]")}}}})
   