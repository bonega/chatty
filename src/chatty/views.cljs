(ns chatty.views
  (:require [re-frame.core :refer [dispatch
                                   subscribe]]
            [reagent.core :as reagent]
            [chatty.utils :refer [human-interval re-pos starts-with?]]))

(defn user-component [users]
  [:div.user-list
   [:ul
    (for [user users]
      ^{:key user} [:li.user user])]])


(defn send-message []
  (let [text (subscribe [:input-text])]
    (when (pos? (count @text))
      (dispatch [:send-message (.getTime (js/Date.))]))))

(defn complete-user-list-component [pos comp-text text users]
  (let [f-users (filter #(starts-with? % (subs comp-text 1)) users)
        match-len (-> comp-text (subs 1) count)
        new-text-fn #(str text (subs % match-len) " ")]
    (for [user f-users]
      ^{:key user} [:li {:on-click #(dispatch [:change-input (new-text-fn user)])} user])))

(defn complete-user-component [users text]
  (let [[[pos comp-text]] (re-pos #"\B@\S*$" text)]
    [:ul#complete-list {:style {:left (str (* 6 pos) "px")}}
     (when pos
       (complete-user-list-component pos comp-text text users))]))

(defn message-field-component []
  (let [text (subscribe [:input-text])
        users (subscribe [:users])]
    (reagent/create-class
     {:component-did-update #(.focus (reagent/dom-node %))
      :reagent-render
      (fn []
        [:input#message-area {:type "text"
                                :value @text
                                :on-change #(dispatch [:change-input (-> % .-target .-value)])
                                :onKeyUp (fn [e] (when (= 13 (.. e -keyCode)) (send-message)))}])})))

(defn input-component []
  (let [text (subscribe [:input-text])
        users (subscribe [:users])]
    (fn []
      [:div.complete-wrapper
       (complete-user-component @users @text)
       [message-field-component]])))


(defn scroll-to-end-of-events []
  (let [element (js/document.getElementById "events-pane")]
    (set! (.-scrollTop element) (.-scrollHeight element))))

(defmulti render-event :event-type)

(defmethod render-event "msg" [event]
  (let [{:keys [user text]} (:value event)
        time (subscribe [:time])]
    (fn [event]
      [:li.msg
       [:div.chat-header
        [:div.heading user]
        [:div.time (human-interval (:timestamp event) @time)]]
       [:p text]])))

(defmethod render-event :disconnect [event]
  [:li.disconnect (str (:value event) " has disconnected")])

(defn event-component []
  (let [events (subscribe [:events])]
    (fn []
      [:div.event-area
       [:ul#events-pane.events
        (for [event @events]
          ^{:key (:timestamp event)} [render-event event])]
       [:div.event-box
        [input-component]
        [:button {:on-click send-message} "send"]]])))


(defn main-component []
  (let [users (subscribe [:users])]
    (fn []
      [:div
       [event-component]
       (user-component @users)])))
