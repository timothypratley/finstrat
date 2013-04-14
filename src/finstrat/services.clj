(ns finstrat.services
  (:require [cheshire.custom :as custom]
            [noir.session :as session]
            [clj-time.format :as format]
            [clj-time.core :as time])
  (:use [finstrat.simulations]
        [finstrat.helpers]
        [finstrat.stats]
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
  (fn [d ^com.fasterxml.jackson.core.JsonGenerator jsonGenerator]
    (.writeString jsonGenerator (readable-date d))))


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
(defpage "/sim/:screens/:symbols/:astart/:aend/:acount/:bstart/:bend/:bcount/:cstart/:cend/:ccount"
  {:keys [screens symbols
          astart aend acount
          bstart bend bcount
          cstart cend ccount]}
  (json
    (let [screens (clojure.string/split screens #"_")
          symbols (clojure.string/split symbols #"_")
          as (apply rangef (map parse-number
                                [astart aend acount]))
          bs (apply rangef (map parse-number
                                [bstart bend bcount]))
          cs (apply rangef (map parse-number
                                [cstart cend ccount]))]
      (for [a as]
        (for [b bs]
          (for [c cs]
            ;; TODO: match sim with symbols... pass in JSON descriptor
            (simulate-apy (map #(cons % screens) symbols) [a b c])))))))

(defpage "/sim/:screens/:symbols/:a/:b/:c"
  {:keys [screens symbols a b c]}
  (json
    (let [symbols (sort (clojure.string/split symbols #"_"))
          screens (clojure.string/split screens #"_")
          ss (map #(cons % screens) symbols)
          states (simulate ss (map parse-number [a b c]))
          _ (println (last states))
          ;; TODO: is there a double cons?
          header (cons "Date" (cons "Cash" symbols))
          rows (for [r states]
                 (cons (r :date)
                       (cons (r :cash)
                             (map #(get-in r [:security % :value]) symbols))))]
      ;; TODO: too much data for area chart
      (take 200 (cons header rows)))))

(defpage "/r"
  {:keys []}
  (json
    (cons ["X" "Y"]
          (plot-move-dist (take 1000 (random-walk 5))))))

