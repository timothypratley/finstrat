(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.mechanics]
        [finstrat.momentum]
        [finstrat.fundamentals]))

(def signal-index {"momentum" momentum})

(defn- calc-weights
  [fs table args]
  (let [average (comp #(/ % (count fs)) +)
        weightss (for [f fs]
                  ; we don't want the empty state
                  (let [results (rest (reductions
                                        (apply partial f args)
                                        {}
                                        table))
                        weights (map :weight results)]
                    (assert (not-any? nil? weights)
                            (str "invalid weight calculated by " f))
                    weights))
        averages (apply map average weightss)]
    (map #(assoc %1 :weight %2) table averages)))

(defn simulate
  "Evaluate how a portfolio would perform over historical data.
   Symbols indicate the security.
   Signals are functions that calculate a weight
   which indicates the probability of the security price rising.
   example: (simulate [[sec1 [sig1 sig2]] [sec2 [sig2 sig3]]])
   All inputs are strings which will be used to look up data and functions."
  [symbol-signals args]
  (for [[symbol & signals] symbol-signals]
    (let [table (get-table symbol)
          fs (map signal-index signals)
          ;TODO: why does this say expected: nil?
          _ (assert (seq fs)
                    "no signals found") 
          weights (calc-weights fs table args)]
      (assert (not-any? nil? (map :weight weights))
              "invalid weight calculated")
      (reductions update weights))))

(defn simulate-apy
  "Calculate the annual percentage yeild for a simulation."
  [symbol-signals args]
  (let [result (simulate symbol-signals args)
        initial (first (first result))
        final (last (first result))
        _ (println initial)
        _ (println final)
        days (t/in-days (t/interval
                          ; TODO fix asymetry due to partial state updates
                          (initial "Date")
                          (final :date)))
        years (/ days 365.242)]
    (Math/pow (final :value) (/ 1 years))))

