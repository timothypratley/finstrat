(ns finstrat.services
  (:require [cheshire.custom :as custom]
            [noir.session :as session]
            [clj-time.format :as format]
            [clj-time.core :as time])
  (:use [finstrat.data]
        ;[finstrat.core]
        ;[clojure.data.csv :only [write-csv]]
        [noir.core]
        [noir.response :only [redirect content-type status]]
        [slingshot.slingshot :only [try+]]))

;TODO: don't need?
(defn readable-date
  [date]
  (format/unparse (if (= (time/year date) (time/year (time/now)))
                    (format/formatter "MMM dd")
                    (format/formatter "MMM dd yyyy"))
                    date))

(custom/add-encoder org.joda.time.DateTime
  (fn [d jsonGenerator]
    (.writeString jsonGenerator (readable-date d))))

(defn tostr-ds-date
  "converts a joda time into a json data source date"
  [date]
  (str "Date(" (time/year date) "," (dec (time/month date)) "," (time/day date) ")"))

;; TODO: add as encoder instead?
(defn fmt
  [value]
  (cond
    (instance? org.joda.time.DateTime value) (tostr-ds-date value)
    :else value))

(defn column-type
  [s]
  (comment DEBUG println "S:" (type s) s)
  (cond
    (number? s) "number"
    (instance? org.joda.time.DateTime s) "date"
    :else "string"))

;there is a version in noir which is not a function
;having a function means we can present in csv or json
;or we could macro it up a notch
(defn json
  "Wraps the response in the json content type
   and generates JSON from the content"
  [content]
  (content-type "application/json; charset=utf-8"
                (custom/generate-string content)))

(comment defn csv
  [filename content]
  (assoc-in 
    (content-type "text/csv"
      (str (doto (java.io.StringWriter.) (write-csv content))))
    [:headers "Content-Disposition"]
    (str "attachment;filename=" filename ".csv")))

(defpage "/json/foo/:symbols/:ts/:te/:tstep/:ds/:de/:dstep"
  {:keys [symbols ts te tstep ds de dstep]}
  ;clojure.string/split symobols
  (let [table (get-table symbols)
        tolerances (range ts te tstep)
        days (range ds de dstep)]
  (json table)))
   ; (reduce (partial step tax tol (partial up d) (partial down d))
    ;            table))))

(defpage "/json/momentum/:symbol"
  {:keys [symbol]}
  (let [table (get-table symbol)
        step identity ;TODO
        price-rising identity
        price-falling identity
        states (reductions (partial step 0.2 0.1 price-rising price-falling)
                           table)
        header ()
        tabulate (fn [state] (map fmt (map state [:date :price :value :note])))
        result (map tabulate states)]
    (json (cons header result))))


