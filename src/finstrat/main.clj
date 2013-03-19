(ns finstrat.main
  (:use [clojure-csv.core]))

;; Run profits and cut losses (Momentum)
;; state machine start in waiting, if x up -> bought if x down -> waiting

(declare init buy sell update)

(def tollerance 0.03)
(defn up
  [preferences state price]
  (> price (+ (state :low) (* (state :low) (preferences :tollerance)))))
(defn down
  [preferences state price]
  (< price (- (state :high) (* (state :high) (preferences :tollerance)))))
  
(defn step [preferences up down state price]
  (comment DEBUG println "STEP: " state price)
  (cond
    (nil? state)
    (init state price)

    (= (state :name) :waiting)
    (if (up preferences state price)
      (buy state price)
      (update state price))

    (= (state :name) :holding)
    (if (down preferences state price)
      (sell state price)
      (update state price))

    :else
      (throw (Exception. "Invalid state name"))))

(defn data
  []
  (map (comp read-string second)
       (reverse (parse-csv (slurp "realsap.csv")))))

;; TODO: plot them
(defn -main [& m]
  (let [prices (data)
        state nil
        inflation 0.023
        ;all (reductions step state prices)
        ]
    (doseq [i [0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (println "Compound interest " i " - inflation " inflation " => " 
               (Math/pow (+ 1 (- i inflation)) (/ (count prices) 12))))
    (println "HELD FOR DURATION: " (/ (last prices) (first prices)))
    (doseq [tollerance [0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (let [result (reduce (partial step {:tollerance tollerance} up down) state prices)]
        (println "POSITIVE " tollerance ": "
                   (+ (result :cash) (* (last prices) (result :shares))))))
    (doseq [tollerance [0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (let [result (reduce (partial step {:tollerance tollerance} down up) state prices)]
        (println "NEGATIVE " tollerance ": "
                   (+ (result :cash) (* (last prices) (result :shares))))))))

(defn update
  [state price]
  (-> state
    (update-in [:low] (partial min price))
    (update-in [:high] (partial max price))))

;; TODO: protect against init occuring multiple times
(defn init
  [state price]
  {:name :waiting
   :high price
   :low price
   :cash 1
   :shares 0})

(defn buy
  [state price]
  (assoc state
    :name :holding
    :low price
    :high price
    :cash 0
    :shares (/ (state :cash) price)))

(defn sell
  [state price]
  (assoc state
    :name :waiting
    :low price
    :high price
    :cash (+ (state :cash) (* price (state :shares)))
    :shares 0))

