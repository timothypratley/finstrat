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
  [signal screen]
  (update-in signal [:screen-state screen]
             ; TODO: args need to be threaded through per screen
             (partial (screen-index screen) [1 1 1])
             signal))

;TODO: args should be per screen
(defn- screen-signal
  "Calculates new weights for each screen on a signal"
  [signal screens]
  (assert (:price signal)
          (str "Should have price for signal " signal))
  (let [signal (reduce update-screen-state signal screens)
        weights (for [[screen state] (signal :screen-state)]
                  (do
                    (assert (state :weight)
                            (str screen " should produce a weight for " signal))
                    (state :weight)))
        _ (assert (seq weights)
                  "Should have calculated some weights")
        average (/ (apply + weights) (count screens))]
    (assoc signal :weight average)))

(defn- screen-step
  [screens-for [prev-date prev-signals] [date signals]]
  (let [symbols (into #{} (concat (keys prev-signals) (keys signals)))
        missing (clojure.set/difference
                 symbols
                 (into #{} (keys signals)))
        ; if there is a signal missing (dates across time-series sometimes mismatch)
        ; then maintain the previous signal
        signals (reduce (fn [sigs sym]
                          (assoc sigs sym
                            ; use previous signal, but update the date
                            (assoc (prev-signals sym)
                              :date date)))
                        signals
                        missing)
        ; copy all the existing screen states to the new signal
        signals (reduce (fn [sigs sym]
                          (assoc-in sigs [sym :screen-state]
                                   (get-in prev-signals [sym :screen-state])))
                        signals
                        symbols)
        _ (assert (every? (comp :price signals) symbols)
                  (str "All signals should have price " signals))
        signals (reduce (fn [sigs sym]
                          (update-in sigs [sym]
                                     screen-signal (screens-for sym)))
                        signals
                        symbols)]
    [date signals]))

(defn- assoc-signal
  [m signal]
  (update-in m [(signal :date)]
             assoc (signal :symbol) signal))

(defn- index-signals
  "Creates a sorted map of dates to maps of symbols to signals."
  [signals]
  (reduce (partial reduce assoc-signal)
          (sorted-map-by t/before?)
          signals))

(defn simulate
  "Evaluate how a portfolio would perform over historical data.
  Symbols indicate the security.
  Screens are functions that calculate a weight
  which indicates the probability of the security price rising.
  example: (simulate [[sec1 [scr1 scr2]] [sec2 [scr3]]])
  ; TODO: [[sec1 sec2] [[scr1 1 2 3] scr2] [sec3 sec4 sec5] [scr6]]
  All inputs are strings which will be used to look up data and functions.

  Screens are functions that calculate weights.
  Weights are calculated through time first,
  the the portfolio is traded according to the weights assigned.
  ; TODO: why not do both each step??"
  [symbol-screens]
  (let [screens-for (into {}
                          (for [[sym & screens] symbol-screens]
                            [sym screens]))
        signals (for [[sym & screens] symbol-screens]
                  (do
                    (assert (seq screens)
                            "Should supply at least one screen")
                    (assert (every? screen-index screens)
                            "Should have only valid screen names")
                    (get-table sym)))
        symbols (map first symbol-screens)
        indexed (index-signals signals)
        ; calculate the screen weights
        rows (map (partial screen-step screens-for)
                  (cons (first indexed) indexed)
                  indexed)
        ; TODO: maybe add the date column at the end instead... or not at all
        ;table (for [[k v] rows]
         ;       (cons k (map v [symbols])))
        ;table (pad-rows table)
        p (portfolio 1000000)]
    ; perform trades according to the weights
    (evaluate p rows)))

(defn simulate-apy
  "Calculate the annual percentage yeild for a simulation."
  [symbol-screens]
  (let [result (simulate symbol-screens)
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

