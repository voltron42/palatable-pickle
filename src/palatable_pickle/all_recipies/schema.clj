(ns palatable-pickle.all-recipies.schema
  (:require [schema/core :as s]))

(s/defschema Recipe
  {s/Str s/Str})