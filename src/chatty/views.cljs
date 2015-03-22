(ns chatty.views
  (:require [re-frame.core :refer [dispatch
                                   subscribe]]))

(defn user-component []
  (let [users (subscribe [:users])]
    (fn []
      [:div.user-list
       [:ul
        (map (partial vector :li.user) @users)]])))


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


(defmulti render-event :event-type)

(defmethod render-event :msg [event]
  (let [{:keys [user text]} (:value event)]
    [:li.msg
     [:div.heading user]
     [:p text]]))

(defmethod render-event :disconnect [event]
  [:li.disconnect (str (:value event) " has disconnected")])

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
