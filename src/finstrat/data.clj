(ns finstrat.data
  (:require [clj-http.client :as client])
  (:use ;[stockings.core]
        [clojure-csv.core]))

(defn realsap
  []
  (map (comp read-string second)
       (parse-csv (slurp "realsap.csv"))))

(defn raw
  [symbol]
  ; TODO: is there a way to only get the Date and Adj Close?
  (let [raw (client/get "http://ichart.finance.yahoo.com/table.csv"
                        {:query-params {:s symbol
                                        :ignore ".csv"}})]
    (map (comp read-string #(nth % 6))
         (rest (parse-csv (raw :body))))))

(comment defn yhoo
  []
  (get-hisotrical-quotes "YHOO" (LocalDate. 2011 4 1) (LocalDate. 2011 5 1)))

