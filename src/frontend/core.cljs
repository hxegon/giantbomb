(ns frontend.core
  (:require [uix.core :as uix :refer [$ defui]]
            [uix.dom :as d]))

(defui app
 []
 ($ :div.font-bold
   ($ :label
      "Label"
      ($ :input {:type :text})
      ($ :button {:type :submit} "submit"))))

(defonce root
  (d/create-root (js/document.getElementById "root")))

(defn render
  []
  (d/render-root ($ app) root))

(defn ^:export init
  []
  (render))
