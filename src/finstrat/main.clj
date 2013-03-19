(ns finstrat.main
  (:use [finstrat.data]))

;; Run profits and cut losses (Momentum)
;; state machine start in waiting, if x up -> bought if x down -> waiting

(declare init buy sell update)

(defn up
  [tollerance state price]
  (> price (+ (state :low) (* (state :low) tollerance))))
(defn down
  [tollerance state price]
  (< price (- (state :high) (* (state :high) tollerance))))
  
(defn step [tax tollerance up down state price]
  (comment DEBUG println "STEP: " state price)
  (cond
    (nil? state)
    (init state price)

    (= (state :name) :waiting)
    (if (up tollerance state price)
      (buy state price)
      (update state price))

    (= (state :name) :holding)
    (if (down tollerance state price)
      (sell state price tax)
      (update state price))

    :else
      (throw (Exception. "Invalid state name"))))

;; TODO: plot them individual (to check validity)
;; and as a group (more fine grained see spikes and clumps to identify sweet spot)
(defn -main [& m]
  (let [prices (reverse (raw "^GSPC"))
        state nil
        inflation 0.023
        tax 0.20
        ;all (reductions step state prices)
        ]
    (doseq [i [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (println "Compound interest " i ": "
               (Math/pow (+ 1 (- (* i (- 1 tax)) inflation)) (/ (count prices) 12))))
    (println "HELD FOR DURATION: "
             (* (/ (last prices) (first prices))
                (- 1 tax)))
    (doseq [tollerance [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10 0.2 0.3 0.5]]
      (let [result (reduce (partial step tax tollerance up down)
                           state prices)]
        (println "POSITIVE " tollerance ": "
                   (+ (result :cash) (* (last prices) (result :units))))))
    (doseq [tollerance [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (let [result (reduce (partial step tax tollerance down up)
                           state prices)]
        (println "NEGATIVE " tollerance ": "
                   (+ (result :cash) (* (last prices) (result :units))))))))

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
   :units 0
   :cost 0})

(defn buy
  [state price]
  (assoc state
    :name :holding
    :low price
    :high price
    :cash 0
    :units (/ (state :cash) price)
    :cost (state :cash)))

(defn sell
  [state price tax]
  (let [proceeds (* price (state :units))
        profit (- proceeds (state :cost))
        proceeds (- proceeds (* profit tax))]
    (assoc state
      :name :waiting
      :low price
      :high price
      :cash (+ (state :cash) proceeds)
      :units 0)))

