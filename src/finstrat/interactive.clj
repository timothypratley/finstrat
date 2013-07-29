(ns finstrat.interactive
  (:use [finstrat.data]
        [incanter core charts stats]))


(multpl)

(def h (house-value xxxx))
(def dates (map first h))

;; TODO from 1996 dates are 2 year gapped!
(def c (changes (map double (reverse (map last h)))))

(view
 (doto (time-series-plot (map first h) (map last h))
   (add-points (rest dates) (map #(* 200000 %) c))))

(view (scatter-plot dates c))
(reverse h)
(view (histogram c))
(mean c)
(Math/pow (mean c) 30)

(def year 1)
(defn buy-and-hold
  "Gross return after 30 years of holding"
  [step]
  (last (take (* 30 year) (iterate step 1))))

(defn create-sample-step
  "Chooses a random step from real data"
  [s]
  (let [lookup (into [] (changes s))]
    #(* % (rand-nth lookup))))

(def ss (create-sample-step c))

(buy-and-hold ss)

(ss 10)

(let [return (repeatedly 1000 #(buy-and-hold ss))]
  (view (histogram return :nbins 50 :title "Simulated 30 year returns"))
  (mean return))

(reduce * c)
(double (/ (first p) (last p)))

(def p (map last h))

(Math/pow 5.21 (/ 1.0 30))

(double (/ 1000 400))

(Math/pow 2.5 (/ 1.0 17))

;; TODO add titles
(let [h (reverse (house-value xxx))
      price (map last h)
      date (map first h)
      c (changes price)
      growth (/ (last price) (first price))
      mid (* growth 0.5 (first price))
      log-price (map #(* 0.1 mid %) (map log (map last h)))
      duration (- (last date) (first date))]
  (view
   (doto (time-series-plot date price)
     (add-lines dates log-price)
     (add-points (rest dates) (map #(* mid %) c))
     (add-lines [(first date) (last date)] [mid mid])))
  (double growth))

(Math/pow 1.7 (/ 1.0 7))