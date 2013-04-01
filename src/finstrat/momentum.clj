(ns finstrat.momentum)

(defn price-rising
  [tolerance period state datum]
  ; TODO: :low should be calculated in here
  ; [or in the simulations]
  ; return state and signal?
  (> (datum "Adj Close") (+ (state :low) (* (state :low) (* 0.01 tolerance)))))

(defn price-falling
  [tolerance period state datum]
  (< (datum "Adj Close") (- (state :high) (* (state :high) (* 0.01 tolerance)))))
  
(defn ma-rising
  [state datum]
  (> (state :sma) (state :lma)))

(defn ma-falling
  [state datum]
  (< (state :sma) (state :lma)))


