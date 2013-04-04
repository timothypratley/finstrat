(ns finstrat.mechanics)

(defn- init
  [state datum]
  (assoc state
    :weight 0  ;;;; TODO: should not set this here??
    :cash 1
    :units 0
    :cost 0))

(defn- buy
  [state datum]
  (assoc state
    :cash 0
    :units (+ (state :units) (/ (state :cash) (datum :price)))
    :cost (+ (state :cost) (state :cash))))

(defn- sell
  [state datum]
  (let [proceeds (* (datum :price) (state :units))
        profit (- proceeds (state :cost))
        proceeds (- proceeds (* profit (state :tax)))]
    (assoc state
      :cash (+ (state :cash) proceeds)
      :units 0
      :cost 0)))


(defn update
  [state datum]
  (let [state (if (not (state :weight)) (init state datum))
        state (cond (pos? (state :weight)) (buy state datum)
                    (neg? (state :weight)) (sell state datum)
                    :else state)]
    (assoc state
           :date (datum :date)
           :value (+ (state :cash)
                     (* (state :units) (datum :price))))))

