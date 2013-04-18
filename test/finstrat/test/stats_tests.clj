(ns finstrat.test.stats_tests
  (:use [finstrat.stats]
        [clojure.test]))

(deftest test-moves
  (is (= (moves [100 101]) [1.0])))

(deftest test-plot-move-dist
  (println "WALK" (take 10 (random-walk 5)))
  (println "PLOT" (plot-move-dist (take 1000 (random-walk 5)))))

