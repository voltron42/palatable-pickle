(ns palatable-pickle.all-recipies.scraper)

(def recipe-url-pattern
  "https://www.allrecipes.com/recipe/{id-int}/{id-keyword}/")

(def old-url-pattern
  "https://www.allrecipes.com/{old-id-keyword}/")

(def list-url-pattern
  "https://www.allrecipes.com/recipes/{id-int}/{list-path-step}/..")

(def recipe-target-classes
  {:details 
   {:item "mm-recipes-details__item"
    :label "mm-recipes-details__label"
    :value "mm-recipes-details__value"}
   })
   
(def recipe-target-xpath
  {:ingredient-item 
   {:item "//.[contains(@class,'mm-recipes-structured-ingredients__list-item')]"
    :ammount "./.[1]"
    :unit "./.[2]"
    :ingredient "./.[3]"}
   :recipe-steps "//div[contains(@class,'mm-recipes-steps')]/ol/li/p"
   :servings "//tr[contains(@class,'mm-recipes-nutrition-facts-label__servings')]/th/span[2]"
   :calories "//tr[contains(@class,'mm-recipes-nutrition-facts-label__calories')]/th/span[2]"
   :nutritional-facts 
   {:row "//tbody[contains(@class,'')]/tr[position() > 1]"
    :full-label "./td[1]"
    :name "./td[1]/span"
    :percent "./td[2]"}})
   
(def menu-xpath "//li[contains(@class,'mntl-fullscreen-nav__sublist-item')]/a")

(def breadcrumb-xpath "//li[contains(@class,'mntl-breadcrumbs__item')]/a")

(def list-page-item-xpath "//a[contains(@class,'mntl-taxonomy-nodes__link ')]")

(def listed-card-xpath 
  {:link "//a[contains(@class,'mntl-card-list-items')]"
   :title "./span[contains(@class,'card__title-text')]"})

(def view-recipe-button-xpath
  "//a[span[contains(text(),'View Recipe')]]")

(def article-title-xpath "//h1")

(comment "")