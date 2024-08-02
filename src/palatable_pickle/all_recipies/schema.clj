(ns palatable-pickle.all-recipies.schema
  (:require [schema.core :as s]))

(s/defschema Link {:title s/Str
                 :href s/Str})

(s/defschema Recipe
  {:title s/Str
   :url s/Str
   :breadcrumb [Link]
   :servings s/Int
   :calories s/Int
   :details {s/Str s/Str}
   :ingredients [{:ammount s/Str
                  :unit s/Str
                  :ingredient s/Str}]
   :steps [s/Str]
   :nutritional-facts [{:name s/Str
                        :ammount s/Str
                        :percent s/Str}]})

(s/defschema List
  {:title s/Str
   :url s/Str
   :breadcrumb [Link]
   :links [Link]})
