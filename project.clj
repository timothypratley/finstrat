(defproject finstrat "1.0.0-SNAPSHOT"
  :description "Technical Analysis"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [lib-noir "0.6.8"]
                 [ring-server "0.2.7"]
                 [hiccup "1.0.4"]
                 [incanter/incanter-core "1.5.2"]
                 [incanter/incanter-excel "1.5.2"]
                 [incanter/incanter-charts "1.5.2"]
                 [clj-http-lite "0.2.0"]
                 [clj-time "0.6.0"]
                 [clojure-csv/clojure-csv "2.0.0-alpha2"]
                 [enlive "1.1.1"]
                 [criterium "0.4.1"]]
  :plugins [[codox "0.6.4"]
            ;[lein-autodoc "0.9.0"]
            ;[lein-marginalia "0.7.1"]
            [lein-ring "0.8.6"]]
  ;:warn-on-reflection true
  :ring {:handler finstrat.server/handler})

