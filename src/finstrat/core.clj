(ns finstrat.core)

(declare init buy sell update)

(defn step
  [buy-signal sell-signal state datum]
  (comment DEBUG println "STEP: " state datum)
  (cond
    (nil? (state :name))
    (init state datum)

    (= (state :name) :waiting)
    (if (buy-signal state datum)
      (buy state datum)
      (update state datum))

    (= (state :name) :holding)
    (if (sell-signal state datum)
      (sell state datum)
      (update state datum))

    :else
      (throw (Exception. "Invalid state name"))))

(defn init
  [state datum]
  (assoc state
    :name :waiting
    :high (datum "Adj Close")
    :low (datum "Adj Close")
    :cash 1
    :units 0
    :cost 0))

(defn update
  [state datum]
  (-> state
    (assoc [:value] (+ (state :cash) (* (state :units) state :cost)))
    (update-in [:low] (partial min (datum "Adj Close")))
    (update-in [:high] (partial max (datum "Adj Close")))))

(defn buy
  [state datum]
  (assoc state
    :name :holding
    :low (datum "Adj Close")
    :high (datum "Adj Close")
    :cash 0
    :units (/ (state :cash) (datum "Adj Close"))
    :cost (state :cash)))

(defn sell
  [state datum]
  (let [proceeds (* (datum "Adj Close") (state :units))
        profit (- proceeds (state :cost))
        proceeds (- proceeds (* profit (state :tax)))]
    (assoc state
      :name :waiting
      :low (datum "Adj Close")
      :high (datum "Adj Close")
      :cash (+ (state :cash) proceeds)
      :units 0)))

