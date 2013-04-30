(ns finstrat.test.helpers_tests
  (:use [finstrat.helpers]
        [clojure.test]))

(deftest test-pad
  (is (= (pad [1 nil nil 2]) [1 1 1 2])))

(deftest test-pad-rows
  (is (= (pad-rows [[1 1] [nil nil] [2 nil]]) [[1 1] [1 1] [2 1]])))

