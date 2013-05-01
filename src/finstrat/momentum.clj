(ns finstrat.momentum)

(defn momentum
  [[tolerance period _] state signal]
  (let [price (signal :price)
        high (or (state :high) price)
        low (or (state :low) price)
        weight (or (state :weight) 0)
        rising (> price (+ low (* low tolerance 0.01)))
        run (and rising (not (pos? weight)))
        falling (< price (- high (* high tolerance 0.01)))
        cut (and falling (not (neg? weight)))]
    (cond
      run (assoc state
                 :high p
                 :weight 1)
      cut (assoc state
                 :low p
                 :weight -1)
      :else (assoc state
                   :low (min low p)
                   :high (max high p)
                   :weight weight))))

(defn hold
  [[target _ _] state signal]
  (assoc state
    :weight target))

; TODO: might make this a namespace search instead of coded?
(def screen-index {"momentum" momentum
                   "hold" hold})
