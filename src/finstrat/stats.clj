(ns finstrat.stats)

(defn percentage-change
  "The percentage change between two values"
  [a b]
  (/ (- b a) a 0.01))

(defn moves
  "The percentage changes between a set of values"
  [data]
  (map percentage-change data (rest data)))

(defn histogram
  [data]
  (let [nbins (Math/sqrt (count data))
        a (apply min data)
        z (apply max data)
        r (- z a)
        bin-size (/ r (dec nbins))
        bin (fn [m x] (update-in m [(Math/floor (* nbins (/ (- x a) r)))] (fnil inc 0)))]
    (reduce bin {} data)))

(defn sample
  [data]
  (let [n (count data)
        resolution (Math/ceil (Math/sqrt n))
        chunk (Math/round (/ n resolution))
        sorted (sort data)
        ; we lose data that is not a factor of size
        ; oh well, let's discard some data from both ends
        ;;; don't have to discard if use a relative height calc
        err (rem n chunk)
        sorted (drop (/ err 2) sorted)]
    (take-nth chunk sorted)))

; given 2 numbers -> width
; height = 1/width/total-width
(defn height
  [a b]
  (/ 1 (- b a)))

(defn mid
  [a b]
  (/ (+ a b) 2))

(defn point
  [a b]
  [(mid a b) (height a b)])

(defn coords
  "s must be a sorted sample"
  [s]
  (let [heights (map height s (rest s))
        total (apply + heights)
        heights (map #(/ % total) heights)
        mids (map mid s (rest s))]
    (map vector mids heights)))

(defn plot-move-dist
  "Plot the frequency of percentage changes between values in a series."
  [data]
  (-> data
     moves
     sample
     coords))

(defn random-walk
  "Returns a lazy seq of new values randomly adjusted from x
   with a slight upward bias."
  [x]
  (lazy-seq
    (cons x (random-walk
              (* x (- 1.026
                      ;; mimicing a normal distribution
                      (apply + (repeatedly 10 #(rand 0.005)))))))))

