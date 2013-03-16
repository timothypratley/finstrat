(ns finstrat.main
  (:use [clojure-csv.core]))

;; Cut losses, run profits (Momentum)
;; state machine start in waiting, if x up -> bought if x down -> waiting
(defn assoc-if
  [comparer new-value old-value]
  (if (comparer new-value old-value)
    new-value
    old-value))

(def tollerance 3)
(defn step [state price]
  (let [state (assoc state :low (min price (state :low)))
        state (assoc state :high (max price (state :high)))]

    (if (= (state :name) :waiting)
      (if (> (- price (state :low)) tollerance)
        (assoc state
               :name :holding
               :price price)  ;bought
        state)
      (if (> (- (state :high) price) tollerance)
        (assoc state
               :name :waiting
               :acc (+ (state :acc) (- price (state :price))))  ;sold
        state))))

(defn -main [& m]
  (let [s (map (comp read-string second) (reverse (parse-csv (slurp "realsap.csv"))))
        price (first s)
        state {:name :waiting
               :high price
               :low price
               :acc 0}]
    (println (reduce step state s))))

