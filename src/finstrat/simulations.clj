(ns finstrat.simulations
  (:use [finstrat.data]
        [finstrat.mechanics]
        [finstrat.momentum]
        [finstrat.fundamentals]))

(declare momentum pe)

(defn simulate
  [r sim symbol & args]
  (let [table (get-table symbol)
        f (condp (partial = sim)
            "PE" pe
            "momentum" momentum)]
    (r (apply f table args)
       {:tax 0.2}
       table)))

(defn momentum
  [table tolerance period t]
  (fn [state datum] 
      (step (partial price-rising tolerance period)
            ;TODO: this is asymetric just for testing...
            ;so we can use 2 dimensions
            (partial price-falling period tolerance)
            state
            datum)))

(defn pe
  [table entry exit t]
  (partial step
           #(< (% "PE") entry)
           #(> (% "PE") exit)))

