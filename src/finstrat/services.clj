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

(defn readable-date
  [date]
  (format/unparse (if (= (time/year date) (time/year (time/now)))
                    (format/formatter "MMM dd")
                    (format/formatter "MMM dd yyyy"))
                    date))

(custom/add-encoder org.joda.time.DateTime
  (fn [d jsonGenerator]
    (.writeString jsonGenerator (readable-date d))))

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
  (let [table (get-table symbols)
        tolerances (range ts te tstep)
        days (range ds de dstep)]
  (json table)))
   ; (reduce (partial step tax tol (partial up d) (partial down d))
    ;            table))))


