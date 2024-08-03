(ns palatable-pickle.all-recipies.schema
  (:require [schema.core :as s]))

(s/defschema Link {:title s/Str
                 :href s/Str})

(s/defschema Page
  {:title s/Str
   :id s/Int
   :key s/Keyword
   :url s/Str
   :menu [Link]
   :breadcrumb [Link]
   :related [Link]})

(s/defschema List
  {:page Page
   :breadcrumb-keys [s/Keyword]
   :list [Link]})

(s/defschema Recipe
  {:page Page
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

