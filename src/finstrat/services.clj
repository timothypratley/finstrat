(ns finstrat.services
  (:require [cheshire.custom :as custom]
            [noir.session :as session]
            [clj-time.format :as format]
            [clj-time.core :as time])
  (:use [finstrat.simulations]
        [finstrat.helpers]
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
  (str "Date("
       (clojure.string/join
         "," (time/year date) (dec (time/month date)) (time/day date) ")")))

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

(comment defpage "/json/foo/:a/:b/:c"
  m
  (let [m (update-many [parse-number :a :b :c])]
    (apply dosomething (map m [:a :b :c]))))

(comment defservice "/json/foo"
  [a :number (compliment nil?) "must be a number"
   b :number (partial > 5) "must be a number less than 5"
   c :date (compliment nil?) "must be a date"]
  (dosomething a b c))

;TODO: why so slow for big numbers?
(defpage "/sim/:sim/:symbols/:xstart/:xend/:xcount/:ystart/:yend/:ycount/:tstart/:tend/:tcount"
  {:keys [sim symbols xstart xend xcount ystart yend ycount tstart tend tcount]}
  (json
    ;clojure.string/split symbols
    (let [xs (apply rangef (map parse-number [xstart xend xcount]))
          ys (apply rangef (map parse-number [ystart yend ycount]))
          ts (apply rangef (map parse-number [tstart tend tcount]))]
      (for [x xs]
        (for [y ys]
          (for [t ts]
            (:value (simulate reduce sim symbols x y t))))))))

(defpage "/sim/:sim/:symbols/:x/:y/:t"
  {:keys [sim symbols x y t]}
  (json
    (let [states (simulate reductions sim symbols x y t)
          header ()
          tabulate (fn [state] (map fmt (map state [:date :price :value :note])))
          result (map tabulate states)]
      (cons header result))))

