(ns chatty.handlers
  (:require [re-frame.core :refer [register-handler
                                   debug
                                   trim-v]]
            [chatty.server]))

(defn log-ex [handler]
  (fn log-ex-handler
    [db v]
    (try
      (handler db v)
      (catch :default e
        (do
          (.error js/console e.stack)
          (throw e))))))


(def standard-middlewares [(if js/goog.DEBUG
                             [log-ex debug trim-v]
                             log-ex debug trim-v)])

(defonce initial-state {:users []
                        :events []
                        :input-text ""
                        :time (.getTime (js/Date.))})

(register-handler
  :initialize
  (fn
    [db _]
    (merge db initial-state)))

(register-handler
 :send-message
 standard-middlewares
 (fn [db [timestamp]]
   (let [msg {:user (:user db) :text (:input-text db) }
         event {:timestamp timestamp :value msg :event-type "msg"}]
     (chatty.server/add-event event)
     (assoc db :input-text ""))))

(register-handler
 :add-events
 standard-middlewares
 (fn [db [events]]
   (update-in db [:events] #(into % events))))

(register-handler
 :add-event
 standard-middlewares
 (fn [db [event]]
   (update-in db [:events] (comp vec conj) event)))

(register-handler
 :add-user
 standard-middlewares
 (fn [db [user]]
   (update-in db [:users] (comp vec conj) user)))

(register-handler
 :update-time
 standard-middlewares
 (fn [db [time]]
   (assoc db :time time)))

(register-handler
 :change-input
 standard-middlewares
 (fn [db [text]]
   (assoc db :input-text text)))

(register-handler
 :disconnect-user
 standard-middlewares
 (fn [db [user timestamp]]
   (update-in db [:events] (comp vec conj)
              {:timestamp timestamp :value user :event-type :disconnect})))

(register-handler
 :login
 standard-middlewares
 (fn [db [user timestamp]]
   (assoc (update-in db [:users] (comp vec conj) user) :user user)))
