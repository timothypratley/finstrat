(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.mechanics]
        [finstrat.momentum]
        [finstrat.fundamentals]))

(def sims {"momentum" momentum})

;; TODO: handle multiple signals
(defn simulate
  [sig symbols & args]
  (for [sym symbols]
    (let [table (get-table sym)
          f (sims sig)]
      (reductions update
                  (reductions (apply partial f args)
                              {:tax 0.2}
                              table)))))

(defn simulated-apr
  [sim symbols & args]
  (let [result (apply simulate sim symbols args)
        initial (first (first result))
        final (first (last result))
        days (t/in-days (t/interval
                          (initial :date)
                          (final :date)))
        years (/ days 365.242)]
    (println "FINAL" final)
    (println "RESULT" result)
    (Math/pow (final :value) (/ 1 years))))

