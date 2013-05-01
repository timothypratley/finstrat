(ns finstrat.test.simulations_tests
  (:use [finstrat.simulations]
        [finstrat.helpers]
        [clojure.test]
        [criterium.core :only [quick-bench]]))

(deftest test-simulate
  (let [result (simulate [["F" "hold"]] [1 1 1])]
    (println (remove nil? (map :comments result)))))

(deftest test-simulate-apy
         (println
           (simulate-apy [["F" "hold"]] [1 1 1]))
         (println
           (simulate-apy [["F" "momentum"]] [1 1 1]))
         #_(quick-bench
           (simulate-apy [["F" "momentum"]] [1 1 1])))
