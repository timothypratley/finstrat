(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.mechanics]
        [finstrat.momentum]
        [finstrat.fundamentals]))

(def sims {"momentum" momentum})

(defn simulate
  [sim symbols & args]
  ;; TODO: combine tables in a more sensible way
  (let [table (apply map merge (map get-table symbols))
        f (sims sim)]
    ;; TODO: buy/sell in response to :weight
    (reductions (apply partial f args)
       {:tax 0.2}
       table)))

(defn simulated-apr
  [sim symbols & args]
  (let [result (apply simulate sim symbols args)
        initial (first result)
        final (last result)
        days (t/in-days (t/interval
                          (initial :date)
                          (final :date)))
        years (/ days 365.242)]
    (Math/pow (final :value) (/ 1 years))))

