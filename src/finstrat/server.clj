(ns finstrat.server
  (:require [noir.server :as server]
            [finstrat.pages]
            [finstrat.services]))

(def handler (server/gen-handler {:ns 'finstrat}))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. ^String (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'finstrat})))

