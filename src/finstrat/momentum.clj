(ns finstrat.momentum)

(defn momentum
  [tolerance period _ local datum]
  (let [p (datum "Adj Close")
        high (or (local :high) p)
        low (or (local :low) p)
        weight (or (local :weight) 0)
        rising (> p (+ low (* low tolerance 0.01)))
        run (and rising (not (pos? weight)))
        falling (< p (- high (* high tolerance 0.01)))
        cut (and falling (not (neg? weight)))]
    (cond
      run (assoc local
                 :high p
                 :weight 1)
      cut (assoc local
                 :low p
                 :weight -1)
      :else (assoc local
                   :low (min low p)
                   :high (max high p)
                   :weight weight))))

