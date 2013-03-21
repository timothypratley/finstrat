(ns finstrat.test.data
  (:use [finstrat.data]
        [clojure.test]))

(deftest foo
         (println (take 5 (get-table "^GSPC"))))
