(ns finstrat.data
  (:require [clj-http.client :as client]
            [incanter.excel :as i]
            [net.cgrand.enlive-html :as html])
  (:use [finstrat.helpers]
        [clojure-csv.core]))

(defn bind-columns
  [header row]
  (zipmap header
          (cons (parse-date (first row))
                (map parse-number (rest row)))))

(defn bind
  [csv]
  ;; TODO: this is slow
  (let [header (first csv)
        data (rest csv)]
    (map (partial bind-columns header) data)))

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
                 "Date" :date}))
       doall))
;; TODO: caching for testing purposes, you will need to restart every day :)
(def get-table (memoize get-table))
;eg: (time (get-table "^GSPC"))

(defn realsap
  []
  (bind (parse-csv (slurp "realsap.csv"))))


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


(defn- parse-fs [fs ds]
  (for [[f d] (map vector fs ds)
        :when f]
    (f d)))

(defn- parse-table
  [table fs]
  {:headings (map html/text (html/select table [:tr :> :th]))
   :data (for [tr (html/select table [:tr])
               :let [tds (html/select tr [:td])
                     tds (map html/text tds)]
               :when (seq tds)]
           (if (seq fs)
             (parse-fs fs tds)
             tds))})

(defn scrape-table
  "Scrapes data from a HTML table at url with CSS selector.
  fs are the parsing functions to use per column, nil indicates skip."
  [url selector fs]
  (parse-table
   (html/select
    (html/html-resource (java.net.URL. url))
    selector)
   fs))

(defn house-value
  "Look up the historic tax assessor valuations for a seattle real estate parcel"
  [parcel]
  (scrape-table
   (str "http://info.kingcounty.gov/Assessor/eRealProperty/Dashboard.aspx?ParcelNbr=" parcel)
   [:table#kingcounty_gov_cphContent_GridViewDBTaxRoll]
   [parse-date nil nil nil parse-money]))
