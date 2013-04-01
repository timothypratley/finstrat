(ns finstrat.fundamentals)

(defn pe-buy
  [state datum]
  (< (datum "PE") 15))

(defn pe-sell
  [state datum]
  (> (datum "PE") 15))

