(defproject finstrat "1.0.0-SNAPSHOT"
  :description "Technical Analysis"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [noir "1.3.0"]
                 [clj-http-lite "0.1.0"]
                 ;[com.fxtlabs/stockings "1.0.0"]
                 [clj-time "0.4.5"]
                 [com.joestelmach/natty "0.6-SNAPSHOT"]
                 [clojure-csv/clojure-csv "2.0.0-alpha2"]
                 [criterium "0.3.1"]]
  :dev-dependencies [[lein-ring "0.7.5"]]
  :warn-on-reflection true
  :ring {:handler finstrat.server/handler}
  :main finstrat.server)

