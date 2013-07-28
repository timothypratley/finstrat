(ns finstrat.test.simulations_tests
  (:use [finstrat.simulations]
        [finstrat.helpers]
        [finstrat.data]
        [clojure.test]
        [criterium.core :only [quick-bench]]))

(deftest test-index-signals
  (#'finstrat.simulations/index-signals
   [(get-table "F")]))

#_(deftest test-screen-row
  (#'finstrat.simulations/screen-step))

(deftest test-simulate
  (let [result (simulate [["F" "hold"]])]
    (println (remove nil? (map :comments result))))
  (let [result (simulate [["F" "hold"]
                          ["X" "hold"]])]
    (println (remove nil? (map :comments result)))))

(deftest test-simulate-apy
         (println
           (simulate-apy [["F" "hold"]]))
         (println
           (simulate-apy [["F" "momentum"]]))
         #_(quick-bench
           (simulate-apy [["F" "momentum"]])))

(run-tests)