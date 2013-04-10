(ns finstrat.test.simulations
  (:use [finstrat.simulations]
        [finstrat.helpers]
        [clojure.test]
        [criterium.core :only [quick-bench]]))

(deftest test-simulate-apy
         (println
           (simulate-apy [["F" "momentum"]] [1 1 1]))
         (quick-bench
           (simulate-apy [["F" "momentum"]] [1 1 1])))

