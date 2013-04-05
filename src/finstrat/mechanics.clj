(ns finstrat.mechanics)

;; Buying at close is impractical, but no less possible than at open.
;; Modelling intra day activity has challenges (data and rules).
;; Using close to open is possible but introduces an unnecessary
;; discontinuity.

(defn- init
  [state datum]
  (assoc state
    :cash 1
    :units 0
    :cost 0))

(defn- buy
  [state datum]
  (assoc state
    :cash 0
    :units (+ (state :units) (/ (state :cash) (datum "Adj Close")))
    :cost (+ (state :cost) (state :cash))))

(defn- sell
  [state datum]
  (let [proceeds (* (datum "Adj Close") (state :units))
        profit (- proceeds (state :cost))
        proceeds (- proceeds (* profit (state :tax)))]
    (assoc state
      :cash (+ (state :cash) proceeds)
      :units 0
      :cost 0)))


(defn update
  [state datum]
  (let [state (if (not (state :cash)) (init state datum) state)
        _ (assert state (str state))
        state (cond (pos? (state :weight)) (buy state datum)
                    (neg? (state :weight)) (sell state datum)
                    :else state)]
    (assoc state
           :date (datum "Date")
           :value (+ (state :cash)
                     (* (state :units) (datum "Adj Close"))))))

