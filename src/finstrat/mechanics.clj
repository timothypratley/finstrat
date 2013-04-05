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
        state (cond (pos? (datum :weight)) (buy state datum)
                    (neg? (datum :weight)) (sell state datum)
                    :else state)]
    (assoc state
           ;TODO: better way don't carry?
           "Adj Close" (datum "Adj Close")
           :date (datum "Date")
           :value (+ (state :cash)
                     (* (state :units) (datum "Adj Close"))))))

