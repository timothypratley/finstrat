(ns finstrat.test.simulations
  (:use [finstrat.simulations]
        [clojure.test]))

(deftest bar
         (println
           (simulate-apy [["F" "momentum"]] [1 1 1])))

