(ns finstrat.data
  (:require [clj-http.client :as client]
            [incanter.excel :as i]
            [net.cgrand.enlive-html :as html])
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
  [sym]
  ; TODO: is there a way to query only the Date and Adj Close?
  (->> (client/get "http://ichart.finance.yahoo.com/table.csv"
                   {:throw-entire-message? true
                    :query-params {:s sym
                                   :ignore ".csv"}})
       :body
       parse-csv
       bind
       (map #(assoc % :symbol sym))
       (map #(clojure.set/rename-keys
              % {"Adj Close" :price
                 "Date" :date}))))
;; TODO: caching for testing purposes, you will need to restart every day :)
(def get-table (memoize get-table))

(defn realsap
  []
  (bind (parse-csv (slurp "realsap.csv"))))

(comment stockings defn yhoo
  []
  (get-hisotrical-quotes "YHOO" (LocalDate. 2011 4 1) (LocalDate. 2011 5 1)))


(defn get-multpl
  []
  ;(let [;response (client/get "http://www.multpl.com/table"
        ;                {:query-params {:f "m"}})
  (let [data (map html/text
               (html/select
                 (html/html-resource (java.net.URL. "http://www.multpl.com/table?f=m"))
                 [:table#datatable :tr :td]))
        pairs (partition 2 data)
        ms (map zipmap (repeat [:date :pe]) pairs)
        ;; TODO: use update-many instead
        ms (map #(update-in % [:date] parse-date) ms)
        ;; TODO: the latest value is marked estimate and does not parse!
        ms (map #(update-in % [:pe] parse-number) ms)]
    ms))

(defn get-aaii-sentiment
  []
  (i/read-xls "http://www.aaii.com/files/surveys/sentiment.xls"))

(defn get-isee
  []
  (i/read-xls " http://www.ise.com/isee#"))


