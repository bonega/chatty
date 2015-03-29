(ns chatty.utils
  (:require [cljs-time.coerce :refer [from-long]]
            [cljs-time.core :refer [] :as clt]))

(defn regex-modifiers
  "Returns the modifiers of a regex, concatenated as a string."
  [re]
  (str (if (.-multiline re) "m")
       (if (.-ignoreCase re) "i")))

(defn re-pos
  "Returns a vector of vectors, each subvector containing in order:
   the position of the match, the matched string, and any groups
   extracted from the match."
  [re s]
  (let [re (js/RegExp. (.-source re) (str "g" (regex-modifiers re)))]
    (loop [res []]
      (if-let [m (.exec re s)]
        (recur (conj res (vec (cons (.-index m) m))))
        res))))

(defn human-interval [t1 t2]
  (let [t1 (from-long t1)
        t2 (from-long t2)
        interval (when (<= t1 t2)
                   (clt/interval t1 t2))]
    (cond
      (nil? interval) "now"
      (> (clt/in-days interval) 6) t1
      (> (clt/in-hours interval) 23)   (str (clt/in-days interval) " days ago")
      (> (clt/in-minutes interval) 59) (str (clt/in-hours interval) " hours ago")
      (> (clt/in-seconds interval) 59) (str (clt/in-minutes interval) " minutes ago")
      :default "now")))
