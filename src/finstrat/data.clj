(ns finstrat.data
  (:require [clj-http.client :as client]
            [incanter.excel :as i]
            [net.cgrand.enlive-html :as html])
  (:use [finstrat.helpers]
        [clojure-csv.core]))

(def parse-date (date-parser "yyyy-MM-dd"))
(defn- bind-columns
  [header row]
  (zipmap header
          (cons (parse-date (first row)) (map parse-number (rest row)))))

(defn- bind
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
  ;; todo cache files
  []
  (bind (parse-csv (slurp "realsap.csv"))))


(defn get-aaii-sentiment
  []
  (i/read-xls "http://www.aaii.com/files/surveys/sentiment.xls"))

(defn get-isee
  []
  (i/read-xls " http://www.ise.com/isee#"))


(defn- parse-fs [fs ds]
  (for [[f d] (map vector fs ds)
        :when f]
    (f d)))

(def contents (partial map (comp first :content)))

(defn- parse-table2
  [table]
  {:headings (map html/text (html/select table [:tr :> :th]))
   :data (for [tr (html/select table [:tr])
               :let [row (contents (html/select tr [:td]))]
               :when (seq row)]
             row)})

(defn- parse-table
  [table fs]
  (for [tr (html/select table [:tr])
        :let [row (contents (html/select tr [:td]))]
        :when (seq row)]
    (parse-fs fs row)))

(defn scrape-table
  "Scrapes data from a HTML table at url with CSS selector.
  fs are the parsing functions to use per column, nil indicates skip."
  [url selector fs]
  (parse-table
   (html/select
    (html/html-resource (java.net.URL. url))
    selector)
   fs))

(defn multpl []
  (scrape-table
   "http://www.multpl.com/table?f=m"
   [:table#datatable]
   [(date-parser "MMM dd, yyyy") parse-number]))

(multpl)

(defn house-value
  "Look up the historic tax assessor valuations for a seattle real estate parcel"
  [parcel]
  (scrape-table
   (str "http://info.kingcounty.gov/Assessor/eRealProperty/Dashboard.aspx?ParcelNbr=" parcel)
   [:table#kingcounty_gov_cphContent_GridViewDBTaxRoll]
   [(date-parser "yyyy") nil nil nil nil nil nil parse-money]))

(defn changes
  [s]
  (map / (rest s) s))

(changes (map last (multpl)))

