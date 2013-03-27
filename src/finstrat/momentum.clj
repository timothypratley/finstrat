(ns finstrat.momentum)

(defn price-rising
  [state price]
  (> price (+ (state :low) (* (state :low) (state :tolerance)))))

(defn price-falling
  [state price]
  (< price (- (state :high) (* (state :high) (state :tolerance)))))
  
(defn ma-rising
  [state price]
  (> (state :sma) (state :lma)))

(defn ma-falling
  [state price]
  (< (state :sma) (state :lma)))


