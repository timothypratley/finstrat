(ns finstrat.main
  (:use [finstrat.core]
        [finstrat.data]
        [finstrat.momentum])
  (:require [clj-time.core :as time]))

;; Run profits and cut losses (Momentum)
;; state machine start in waiting
;; if x up -> bought if x down -> waiting
;; TODO: https://github.com/cdorrat/reduce-fsm



;; TODO: plot them individual (to check validity)
;; and as a group (more fine grained see spikes and clumps to identify sweet spot)
(defn -main [& m]
  (let [table (reverse (get-table "^GSPC"))
        prices (map #(% "Adj Close") table)
        ;TODO: avoid truncation
        years (time/in-years (time/interval
                               ((first table) "Date")
                               ((last table) "Date")))
        inflation 0.023
        tax 0.20
        first-price ((first table) "Adj Close")
        last-price ((last table) "Adj Close")
        ;all (reductions step state table)
        ]
    (println "Years: " years)
    (println "Inflated by: " (Math/pow (+ 1 inflation) years))
    (doseq [i [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (println "Compound interest " i ": "
               (Math/pow (+ 1 (* i (- 1 tax))) years)))
    (println "HELD FOR DURATION: "
             (* (/ last-price first-price)
                (- 1 tax)))
    (doseq [tolerance [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10 0.2 0.3 0.5]]
      (let [state {:tax tax
                   :tolerance tolerance}
            result (reduce (partial step price-rising price-falling)
                           state table)]
        (println "POSITIVE " tolerance ": "
                   (+ (result :cash)
                      (* last-price (result :units))))))
    (doseq [tolerance [0.005 0.01 0.02 0.03 0.04 0.05 0.06 0.07 0.08 0.09 0.10]]
      (let [state {:tax tax
                   :tolerance tolerance}
            result (reduce (partial step price-falling price-rising)
                           state prices)]
        (println "NEGATIVE " tolerance ": "
                   (+ (result :cash)
                      (* (last prices) (result :units))))))))

