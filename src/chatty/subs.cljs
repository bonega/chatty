(ns chatty.subs
  (:require-macros [reagent.ratom :refer [reaction]])   ;; remove for v0.2.0-alpha2
  (:require [re-frame.core :refer [register-sub]]))

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

(register-sub
 :user
 (fn [db _]
   (reaction (:user @db))))

(register-sub
 :time
 (fn [db _]
   (reaction (:time @db))))
