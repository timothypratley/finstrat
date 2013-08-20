(ns finstrat.test.data_tests
  (:use [finstrat.data]
        [clojure.test]))

; too slow for normal test runs
(deftest foo
         (println (take 5 (get-table "^GSPC")))
         (println (take 5 (get-table "F"))))

(deftest test-multpl
  (println "MULTPL" (take 20 (multpl))))
