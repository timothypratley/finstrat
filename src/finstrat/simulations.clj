(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.portfolio]
        [finstrat.momentum]
        [finstrat.helpers]))


(defn- update-screen-state
  [args signal screen]
  (update-in signal [:screen-state screen]
             (screen_index screen) signal args))

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
        _ (assert (seq weight)
                  "Should have calculates some weights")
        average (/ (apply + weights) (count screens))]
    (assoc signal :weight average)))

(defn- screen-row
  [[date signals] args]
  (map (partial screen-signal args) (map signals symbols)))

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
        screen-map (reduce (fn [m ss]
                             (assoc m (first ss) (map screen-index (rest ss))))
                           {}
                           symbol-screens)
        indexed (index-signals signals)
        result (reduce screen-row indexed))
        ;TODO: maybe add the date column at the end instead... or not at all
        table (for [[k v] sparse]
                (cons k (map v [symbols])))
        table (pad-rows table)
        table (map (fn [row]
                     (cons (first row)
                           (map (partial calc-weight args screen-map) (rest row))))
                   table)]
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

