(ns chatty.views
  (:require [re-frame.core :refer [dispatch
                                   subscribe]]
            [reagent.core :as reagent :refer [atom]]
            [chatty.utils :refer [human-interval re-pos starts-with?]]))

(def ctg (reagent/adapt-react-class (aget js/React "addons" "CSSTransitionGroup")))


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
     nil
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
       [complete-user-component @users @text]
       [message-field-component]])))


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

(defn events-pane []
  (let [events (subscribe [:events])
        at-bottom (atom true)]
    (reagent/create-class
     {:component-did-update #(let [element (reagent/dom-node %)]
                               (when @at-bottom
                                 (set! (.-scrollTop element) (.-scrollHeight element))))
      :component-will-update #(let [e (reagent/dom-node %)
                                    to-top (.-scrollTop e)
                                    height (.-scrollHeight e)
                                    offset-height (.-offsetHeight e)
                                    to-bottom (- height (+ offset-height to-top))]
                                (reset! at-bottom (zero? to-bottom)))
      :reagent-render (fn []
                         [ctg {:transitionName "event" :component "ul" :className "events"}
                           (for [event @events]
                             ^{:key (:timestamp event)} [render-event event])])})))

(defn event-component []
  [:div.event-area
       [events-pane]
       [:div.event-box
        [input-component]
        [:button {:on-click send-message} "send"]]])


(defn main-component []
  (let [users (subscribe [:users])]
    (fn []
      [:div
       [event-component]
       [user-component @users]])))
