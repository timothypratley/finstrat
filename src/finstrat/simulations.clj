(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.portfolio]
        [finstrat.momentum]
        [finstrat.helpers]))

; Screen states are associated to the signal.
; The screens are maintaining their own 'state' which is what we are calculating.
; We are reducing over new data in the form of future signals...
; but for convenience the calculated state gets associate to the signal and accumulated
; so that we have the full history of what occured in the simulation.
(defn- update-screen-state
  [args signal screen]
  (update-in signal [:screen-state screen]
             (screen-index screen) signal args))

;TODO: args should be per screen
(defn- screen-signal
  "Calculates new weights for each screen on a signal"
  [args signal screens]
  (let [signal (reduce (partial update-screen-state args) signal screens)
        weights (for [[screen state] (signal :screen-state)]
                  (do
                    (assert (state :weight)
                            (str screen " should produce a weight for " signal))
                    (state :weight)))
        _ (assert (seq weights)
                  "Should have calculated some weights")
        average (/ (apply + weights) (count screens))]
    (assoc signal :weight average)))

(defn- screen-row
  [args current [date signals]]
  ; if there is a signal missing (dates across time-series sometimes mismatch)
  ; then maintain the previous signal for this step,
  ; otherwise copy all the existing screen states to the new signal
  (let [signals (reduce #(if (nil? (get-in [(%1 :symbol (%2 :symbol))]))
                           (assoc-in %1 :symbol (%2 :symbol) %2)
                           %1)
                        signals
                        current)
        signals (reduce #(assoc-in %1 [(%1 :symbol) :screen-state] (get-in [(%2 :symbol) :screen-state]) signals current))]
    (map (partial screen-signal args) (map #(or %1 %2) signals current))))

(defn- assoc-signal
  [m signal]
  (update-in m [(signal :date)]
             assoc (signal :symbol) signal))

(defn- index-signals
  "Creates a sorted map of dates to maps of symbols to signals."
  [signals]
  (reduce (partial reduce assoc-signal)
          (sorted-map-by t/after?)
          signals))

(defn simulate
  "Evaluate how a portfolio would perform over historical data.
  Symbols indicate the security.
  Screens are functions that calculate a weight
  which indicates the probability of the security price rising.
  example: (simulate [[sec1 [sig1 sig2]] [sec2 [sig2 sig3]]])
  All inputs are strings which will be used to look up data and functions."
  [symbol-screens args]
  (let [signals (for [[s & screens] symbol-screens]
                  (do
                    (assert (seq screens)
                            "Should supply at least one screen")
                    (assert (every? screen-index screens)
                            "Should have only valid screen names")
                    (->> (get-table s)
                         (map #(clojure.set/rename-keys % {"Adj Close" :price, "Date" :date}))
                         (map #(assoc % :symbol s)))))
        symbols (map first symbol-screens)
        indexed (index-signals signals)
        ; calculate the screen weights
        result (reduce (partial screen-row args) indexed)
        ; TODO: maybe add the date column at the end instead... or not at all
        table (for [[k v] result]
                (cons k (map v [symbols])))
        table (pad-rows table)]
    ; perform trades according to the weights
    (evaluate 100000 table)))

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

