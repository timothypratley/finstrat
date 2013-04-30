(ns finstrat.simulations
  (:require [clj-time.core :as t])
  (:use [finstrat.data]
        [finstrat.portfolio]
        [finstrat.momentum]
        [finstrat.helpers]))

;TODO: args should be per screen
(defn- calc-weight
  [args screen-map signal]
  (let [screens (screen-map (signal :symbol))
        weights (for [f screens]
                  (let [w (f args signal)]
                    (assert w
                            (str "Should get a weight from " f))
                    w))
        average (/ (apply + weights) (count screens))]
    (assoc signal :weight average)))

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
                    (map #(assoc (clojure.set/rename-keys % {"Adj Close" :price "Date" :date})
                                 :symbol s)
                         (get-table s))))
        symbols (for [[s & screens] symbol-screens]
                  s)
        screen-map (reduce (fn [m ss]
                             (assoc m (first ss) (map screen-index (rest ss))))
                           {}
                           symbol-screens)
        ; index events by date/symbol
        sparse (reduce (fn update-signal [m signal]
                         (reduce (fn update-event [m event]
                                   (update-in m [(event :date)] assoc (event :symbol) event))
                                 m
                                 signal))
                       (sorted-map-by t/after?)
                       signals)
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

