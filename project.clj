(defproject finstrat "1.0.0-SNAPSHOT"
  :description "Technical Analysis"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [noir "1.3.0"]
                 [incanter/incanter-core "1.4.1"]
                 [incanter/incanter-excel "1.4.1"]
                 [incanter/incanter-charts "1.4.1"]
                 [clj-http-lite "0.1.0"]
                 [clj-time "0.4.5"]
                 [clojure-csv/clojure-csv "2.0.0-alpha2"]
                 [enlive "1.1.1"]
                 [criterium "0.3.1"]]
  :dev-dependencies [[lein-ring "0.7.5"]]
  :warn-on-reflection true
  :ring {:handler finstrat.server/handler}
  :main finstrat.server)

