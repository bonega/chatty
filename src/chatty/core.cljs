(ns chatty.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   subscribe
                                   debug]]))


(defonce initial-state {:users [] :events [] :input-text ""})

(register-handler
  :initialize
  (fn
    [db _]
    (merge db initial-state)))

(register-sub
 :events
 (fn [db _]
   (reaction (:events @db))))

(register-sub
 :users
 (fn [db _]
   (reaction (:users @db))))

(register-sub
 :input-text
 (fn [db _]
   (reaction (:input-text @db))))

(register-handler
 :send-message
 debug
 (fn [db [_ timestamp]]
   (assoc (update-in db [:events] (comp vec conj) {:timestamp timestamp :text (:input-text db) :event-type :msg})
             :input-text "")))

(register-handler
 :add-event
 debug
 (fn [db [_ event]]
   (update-in db [:events] (comp vec conj) event)))

(register-handler
 :add-user
 debug
 (fn [db [_ user]]
   (update-in db [:users] (comp vec conj) user)))

(register-handler
 :change-input
 debug
 (fn [db [_ text]]
   (assoc db :input-text text)))

(register-handler
 :disconnect-user
 debug
 (fn [db [_ user timestamp]]
   (update-in db [:events] (comp vec conj) {:timestamp timestamp :text (str user " disconnected") :event-type :info})))

(defn user-component []
  (let [users (subscribe [:users])]
    (fn []
      [:div.user-list
       [:ul
        (map (partial vector :li) @users)]])))


(defn send-message []
  (let [text (subscribe [:input-text])]
    (when (pos? (count @text))
      (dispatch [:send-message (.getTime (js/Date.))]))))

(defn input-component []
  (let [text (subscribe [:input-text])]
    (fn []
      [:div.input {:onKeyUp (fn [e] (when (= 13 (.. e -keyCode)) (send-message)))}
       ""
       [:input {:type "text"
                :value @text
                :on-change #(dispatch [:change-input (-> % .-target .-value)])}]])))


(defn render-event [event]
  (let [{:keys [timestamp text event-type]} event]
    [(keyword (str "li." (name event-type))) text]))

(defn event-component []
  (let [events (subscribe [:events])]
    (fn []
      [:div.event-area
       [:div
        [:ul.events
         (map render-event @events)]]
       [:div.event-box
        [input-component]
        [:button {:on-click send-message} "send"]
        ]])))


(defn main-component []
  [:div
   [event-component]
   [user-component]])

(defn main []
  (reagent/render [main-component]
                  (js/document.getElementById "app")))

(dispatch [:initialize])
