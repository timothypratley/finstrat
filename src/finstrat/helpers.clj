(ns finstrat.helpers
  (:require [clj-time.coerce :as coerce]))

(defn rangef
  "Returns a sequence of n numbers from start to end inclusive."
  [start end n]
  (if (> n 1)
    (for [i (range 0 n)]
      (+ start
         (* i (/ (- end start) (dec n)))))
    [start]))
(comment (rangef 0 0.1 11) -> [0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1])

(defn- update-keys
  [m [f & kvs]]
  (reduce (fn [m kv] (update-in m kv f))
          m
          kvs))

(defn update-many
  "Returns m with values updated by a sequence of function keylists
   (update-many m [f [:c :x]])
   is equivalent to (update-in m [:c :x] f)
   but you can include multiple expressions:
   (update-many m [f [:a] [:c :x]]
                  [g [:b :y] [:d]])"
  [m & more]
  (reduce update-keys m more))

;; TODO: this is really aweful, trying to avoid floating point precision
(defn two-dec
  [d]
  (/ (Math/round (* 100.0 d)) 100.0))

(defn parse-date [date-string]
  (let [groups (.parse (com.joestelmach.natty.Parser.) date-string)]
    (if-not (empty? groups)
      (coerce/from-date (first (.getDates ^com.joestelmach.natty.DateGroup (first groups)))))))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [^String s]
  (if (re-find #"^-?\d+\.?\d*([Ee]\+\d+|[Ee]-\d+|[Ee]\d+)?$" (.trim s))
    (read-string s)))

(defn- update-keys
  [m [f & kvs]]
  (reduce (fn [m kv] (update-in m kv f))
          m
          kvs))

(defn update-many
  "Returns hashmap m with values updated
   by a sequence of [function & keylists]
   (update-many m [f [:c :x]])
   is equivalent to (update-in m [:c :x] f)
   but you can include multiple expressions:
   (update-many m [f [:a] [:c :x]]
                  [g [:b :y] [:d]])"
  [m & more]
  (reduce update-keys m more))

(defn- max-maps*
  [k s m]
  (let [current ((first s) k)
        val (k m)]
    (cond
      (> current val) s
      (> val current) #{m}
      :else (conj s m))))

(defn max-maps
  "Returns the set of maps for which (m k), a number, is greatest
   from a sequence of maps."
  ([k] #{})
  ([k m & more]
   (reduce (partial max-maps* k) #{m} more)))

(defn reduct
  [val f coll]
  (reduce f val coll))

