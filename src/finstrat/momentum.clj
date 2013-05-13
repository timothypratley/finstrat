(ns finstrat.momentum)

(defn momentum
  [[tolerance period _] state signal]
  (let [price (signal :price)
        high (or (:high state) price)
        low (or (:low state) price)
        weight (or (:weight state) 0)
        rising (> price (+ low (* low tolerance 0.01)))
        run (and rising (not (pos? weight)))
        falling (< price (- high (* high tolerance 0.01)))
        cut (and falling (not (neg? weight)))]
    (cond
      run (assoc state
                 :high price
                 :weight 1)
      cut (assoc state
                 :low price
                 :weight -1)
      :else (assoc state
                   :low (min low price)
                   :high (max high price)
                   :weight weight))))

(defn hold
  [[target _ _] state signal]
  (assoc state
    :weight target))

; TODO: might make this a namespace search instead of coded?
(def screen-index {"momentum" momentum
                   "hold" hold})
