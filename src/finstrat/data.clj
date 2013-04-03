(ns finstrat.data
  (:require [clj-http.client :as client])
  (:use [finstrat.helpers]
        ;[stockings.core]
        [clojure-csv.core]))

(defn bind-columns
  [header row]
  (zipmap header
          (cons (parse-date (first row)) (map parse-number (rest row)))))

(defn bind
  [csv]
  ;; TODO: Yes this is very slow (but cached) - reflecting
  ;; TODO: does doall make any difference?
  (time (doall (let [header (first csv)
        data (rest csv)]
    (map (partial bind-columns header) data)))))

(defn get-table
  [symbol]
  ; TODO: is there a way to query only the Date and Adj Close?
  (let [response (client/get "http://ichart.finance.yahoo.com/table.csv"
                        {:query-params {:s symbol
                                        :ignore ".csv"}})
        csv (parse-csv (response :body))]
    (bind csv)))
;; caching for testing purposes, you will need to restart every day :)
(def get-table (memoize get-table))

(defn realsap
  []
  (bind (parse-csv (slurp "realsap.csv"))))

(comment stockings defn yhoo
  []
  (get-hisotrical-quotes "YHOO" (LocalDate. 2011 4 1) (LocalDate. 2011 5 1)))

