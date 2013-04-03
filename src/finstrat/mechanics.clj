(ns finstrat.mechanics)

(declare init buy sell update)

(defn step
  [buy-signal sell-signal state datum]
  (comment DEBUG println "STEP: " state datum)
  ;TODO: simplify
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
         ;; TODO: these don't belong anymore
         ;; they should go in the signal itself
    :high (datum "Adj Close")
    :low (datum "Adj Close")
    :cash 1
    :units 0
    :cost 0))

;TODO run this always and simplify b/s
(defn update
  [state datum]
  (assoc state
    :date (datum "Date")
    :value (+ (state :cash) (* (state :units) (datum "Adj Close")))))

(defn buy
  [state datum]
  (assoc state
    :name :holding
    :value (state :cash)
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
      :value (+ (state :cash) proceeds)
      :cash (+ (state :cash) proceeds)
      :units 0)))
