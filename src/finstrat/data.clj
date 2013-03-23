(ns finstrat.data
  (:require [clj-http.client :as client]
            [clj-time.coerce :as coerce])
  (:use ;[stockings.core]
        [clojure-csv.core]))

(defn parse-date [date-string]
  (let [groups (.parse (com.joestelmach.natty.Parser.) date-string)]
    (if-not (empty? groups)
      (coerce/from-date (first (.getDates (first groups)))))))

(defn parse-number
  "Reads a number from a string. Returns nil if not a number."
  [s]
  (if (re-find #"^-?\d+\.?\d*([Ee]\+\d+|[Ee]-\d+|[Ee]\d+)?$" (.trim s))
    (read-string s)))

(defn bind-columns
  [header row]
  (zipmap header
          (cons (parse-date (first row)) (map parse-number (rest row)))))

(defn bind
  [csv]
  (let [header (first csv)
        data (rest csv)]
    (map (partial bind-columns header) data)))

(defn get-table
  [symbol]
  ; TODO: is there a way to query only the Date and Adj Close?
  (let [response (client/get "http://ichart.finance.yahoo.com/table.csv"
                        {:query-params {:s symbol
                                        :ignore ".csv"}})
        csv (parse-csv (response :body))]
    (bind csv)))

(defn realsap
  []
  (bind (parse-csv (slurp "realsap.csv"))))

(comment stockings defn yhoo
  []
  (get-hisotrical-quotes "YHOO" (LocalDate. 2011 4 1) (LocalDate. 2011 5 1)))

