(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.portfolio]
        [finstrat.momentum]
        [finstrat.fundamentals]))

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

(defn- weight-one
  [s screens args]
  (println "SCREENS" screens)
  (let [table (get-table s)
        fs (remove nil? (map screen-index screens))
        ; TODO: why does assert say expected: nil?
        _ (assert (seq fs)
                  "no screens found") 
        weights (calc-weights fs table args)]
    (assert (not-any? nil? (map :weight weights))
            "invalid weight calculated")
    weights))

(defn simulate
  "Evaluate how a portfolio would perform over historical data.
   Symbols indicate the security.
   Screens are functions that calculate a weight
   which indicates the probability of the security price rising.
   example: (simulate [[sec1 [sig1 sig2]] [sec2 [sig2 sig3]]])
   All inputs are strings which will be used to look up data and functions."
  [symbol-screens args]
  (evaluate 100000
            ;; TODO: insert missing dates (also wtf with UTC?)
            ;; 1. find the max min date
            ;; 2. step through recycling missing parts
            ;; or sorted map inserts
            ;; or don't evaluate seqences, step through row at a time
    (reverse
      (apply map vector (for [[s & screens] symbol-screens]
                          (reverse (weight-one s screens args)))))))

(defn simulate-apy
  "Calculate the annual percentage yeild for a simulation."
  [symbol-screens args]
  (let [result (simulate symbol-screens args)
        initial (first result)
        final (last result)
        days (t/in-days (t/interval
                          (initial :date)
                          (final :date)))
        years (/ days 365.242)
        growth (/ (final :value) 100000)
        yearly (Math/pow growth (/ 1 years))
        percent (* 100 (- yearly 1))]
    percent))

