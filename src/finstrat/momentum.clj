(ns finstrat.momentum)

(defn price-rising
  [state datum]
  (> (datum "Adj Close") (+ (state :low) (* (state :low) (state :tolerance)))))

(defn price-falling
  [state datum]
  (< (datum "Adj Close") (- (state :high) (* (state :high) (state :tolerance)))))
  
(defn ma-rising
  [state datum]
  (> (state :sma) (state :lma)))

(defn ma-falling
  [state datum]
  (< (state :sma) (state :lma)))


