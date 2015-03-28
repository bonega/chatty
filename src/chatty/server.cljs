(ns chatty.server
  (:require [matchbox.core :as m]
            [re-frame.core :refer [dispatch]]))

;; cljs
(enable-console-print!)
(def safe-prn (partial prn "> "))

(def c (m/connect "https://crackling-inferno-3261.firebaseio.com"))

(def events (m/get-in c [:events]))
(m/listen-to events :child-added #(dispatch [:add-event (second %)]))
(m/listen-to events :child-added #(safe-prn "listen> " %));(vals (second %))))

(defn add-event [event]
  (m/conj! events event))
