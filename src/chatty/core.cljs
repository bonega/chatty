(ns chatty.core
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [chatty.views]
            [chatty.handlers]
            [chatty.subs]))



(defn main []
  (reagent/render [chatty.views/main-component]
                  (js/document.getElementById "app")))

(dispatch [:initialize])
(dispatch [:login "bonega"])
