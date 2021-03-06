(ns finstrat.helpers
  (:use [clj-time.coerce]
        [clj-time.format]
        [clj-time.core]))


(defn readable-date
  [date]
  (unparse (if (= (year date) (year (now)))
                    (formatter "MMM dd")
                    (formatter "MMM dd yyyy"))
                    date))

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

;; TODO: this is really aweful
(defn two-dec
  "Avoid over precision when sending JSON"
  [d]
  (/ (Math/round (* 100.0 d)) 100.0))

(defn date-parser
  "Create a date parsing function for a format"
  [format-string]
  #(parse (formatter format-string) %))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [^String s]
  (if (re-find #"^-?\d+\.?\d*([Ee]\+\d+|[Ee]-\d+|[Ee]\d+)?$" (.trim s))
    (read-string s)))

(defn parse-money
  [^String s]
  (-> s
      (clojure.string/replace "$" "")
      (clojure.string/replace "," "")
      parse-number))
;; (parse-money "$4,123.55")

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
  "Passing arguments in a different order for convenient capture with partial"
  [val f coll]
  (reduce f val coll))

(defn pad
  ([s] (pad (first s) s))
  ([prev s]
   (if (seq s)
     (let [curr (or (first s) prev)]
       (cons curr (lazy-seq (pad curr (rest s))))))))

(defn pad-rows
  ([s] (pad-rows (first s) s))
  ([prev s]
   (if (seq s)
     (let [curr (map #(or %1 %2) (first s) prev)]
       (cons curr (lazy-seq (pad-rows curr (rest s))))))))

