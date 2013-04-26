(ns finstrat.portfolio
  (:use [clojure.test]))

;; TODO: use dataflow for :values as they are dependent
;; What are the implications of floating point precision?
;; Buying at close is impractical, but no less possible than at open.
;; Modelling intra day activity has challenges (data and rules).
;; Using close to open is possible but introduces an unnecessary
;; discontinuity.

(defn- buy
  [p signal spend]
  (assert (pos? spend)
          "Should only buy positive values")
  (assert (>= (p :cash) spend)
          "Should only buy to liquidity limit")
  (let [price (signal :price)
        symbol (signal :symbol)
        _ (assert (pos? price)
                  "Should not buy free securities")
        units (int (/ spend price))
        spend (* units price)]
    (-> p
      (update-in [:cash] - spend)
      (update-in [:comments] (fnil conj [])
                 (str "bought " units " " symbol
                      " @ " price " for $" spend))
      (update-in [:security symbol]
                 #(-> %
                    (update-in [:units] + units)
                    (update-in [:cost] + spend))))))
; TODO: move these out... they don't run here
(deftest test-buy
         (let [p {:cash 10
                  :security {"X" {:units 0
                                  :cost 0}}}
               signal {:symbol "X"
                       :price 10}
               p (buy p signal 10)]
           (is (= (get-in p [:security "X" :units]) 1))))

(defn- cost-of
  [security units]
  (let [held (security :units)
        cost-per-unit (/ (security :cost) held)]
    (* units cost-per-unit)))

(defn- tax
  [p signal units]
  (let [security (get-in p [:security (signal :symbol)])
        cost (cost-of security units)
        value (* units (signal :price))
        profit (- value cost)]
    (* profit (p :tax))))
(deftest test-tax
         (let [p {:tax 0.2
                  :security {"X" {:units 1
                                  :cost 10}}}
               signal {:symbol "X"
                       :price 11}]
           (is (= (tax p signal 1) 0.2))))


; TODO: tax is only paid in April if not withheld
(defn- sell
  ([p signal]
   (sell p signal nil))
  ([p signal value]
   (let [symbol (signal :symbol)
         security (get-in p [:security symbol])
         held (security :units)
         price (signal :price)
         ;_ (println "SECURITY" security)
         ;_ (println "SIGNAL" signal)
         ;_ (println "HELD" held)
         ;_ (println "PRICE" price)
         value (or value (* price held))
         _ (assert (pos? value)
                   "Should only sell positive values")
         units (if (= price 0)
                 held
                 (int (/ value price)))
         value (* units price)
         _ (assert (and (<= units held) (pos? held))
                   "Should only sell securities held in the portfolio")
         cost (cost-of security units)
         proceeds (- value (tax p signal units))]
     (-> p
       (update-in [:cash] + proceeds)
       (update-in [:comments] (fnil conj [])
                  (str "sold " units " " symbol
                       " @ " price " for $" proceeds))
       (update-in [:security (signal :symbol)]
                  #(-> %
                     (update-in [:units] - units)
                     (update-in [:cost] - cost)))))))
(deftest test-sell
         (let [p {:cash 0
                  :tax 0.2
                  :security {"X" {:units 1
                                  :cost 10}}}
               signal {:symbol "X"
                       :price 11}
               p (sell p signal 11)]
         (is (= (p :cash) 10.8))))

(defn- raw-value
  [p signal]
  (let [units (get-in p [:security (signal :symbol) :units])]
    (* units (signal :price))))

(defn- taxed-value
  "Tax adjusted current value of a security as of signal in a portfolio p"
  [p signal]
  (let [units (get-in p [:security (signal :symbol) :units])]
    (if (pos? units)
      (- (raw-value p signal)
         (tax p signal units))
      0)))

(defn- ballance
  "Given a portfolio that is only invested in signals,
   evaluate how many securities should be bought or sold
   to maintain a relatively equal weighting between them."
  [p signals]
  (let [total (apply + (p :cash)
                 (map (partial raw-value p) signals))
        target (/ total (count signals))
        ;only buy and sell if a long way off the target allocation
        tolerance 0.2
        gap (fn [signal]
              (- target (raw-value p signal)))
        gap-proportion (fn [signal]
                         (/ (gap signal) target))
        sell? (fn [signal]
                (< (gap-proportion signal) (- tolerance)))
        sell-list (filter sell? signals)
        trim (fn [p signal]
               (sell p signal (- (gap signal))))
        ;trim securities that are over
        p (reduce trim p sell-list)
        buy? (fn [signal]
               (> (gap-proportion signal) tolerance))
        buy-list (filter buy? signals)
        top-up (fn [p signal]
                 (buy p signal
                      (min (p :cash) (gap signal))))
        ;top up securities that are under
        p (reduce top-up p buy-list)]
    p))

(defn- reweight
  "Given a portfolio, buy and sell according to signal weights
   such that the resulting portfolio is prudently invested."
  [p signals]
  (let [sorted (sort-by :weight signals)
        best ((last sorted) :weight)
        ; http://www.investopedia.com/terms/l/long.asp
        long? #(>= (% :weight) best 0.1)
        longs (take-while long? (reverse sorted))
        ; TODO: try ballancing into only the newest/mid/oldest outperforms
        ; http://www.investopedia.com/terms/s/short.asp
        ;worst ((first sorted) :weight)
        ;short? #(<= (% :weight) worst 0.1)
        ;shorts (take-while short? sorted)
        sells (remove (set longs) signals)
        sells (filter #(pos? (get-in p [:security (% :symbol) :units])) sells)
        p (reduce sell p sells)
        p (if (empty? longs)
            p
            (ballance p longs))]
    p))

(defn- portfolio
  "Create a portfolio"
  [cash signals]
  {:cash cash
   :tax 0.2
   :security (zipmap (map :symbol signals)
                     (repeat {:units 0
                              :cost 0}))
   :value cash})

(defn- update-security
  [p signal]
  (let [price (signal :price)] 
    (-> p
      (update-in [:security (signal :symbol)]
                 (fn [security]
                   (let [units (security :units)]
                     (-> security
                       (assoc :price price)
                       (assoc :value (taxed-value p signal)))))))))

(defn- update
  [p signals]
  ; TODO: nicer way to remap "Adj Close" to :price, "Date" to :date
  ; or do it in data? (price might be a function of bid/ask/open/close)
  (let [signals (map #(clojure.set/rename-keys % {"Adj Close" :price
                                                  "Date" :date})
                     signals)
        p (dissoc p :comments)
        p (reweight p signals)
        p (reduce update-security p signals)
        date ((first signals) :date)]
    ;; invariant - TODO: how to do invariant in Clojure (entry, during, exit)
    (assert (not (neg? (p :cash)))
            "Cash should not be overdrawn")
    #_(assert (every? #(= date (% :date)) (rest signals))
            "Signals should all have the same date")
    (assoc p
           :date date
           :value (apply + (p :cash)
                         (map (partial taxed-value p) signals)))))

(defn evaluate
  "Evaluate the performance of a portfolio over historical data.
   stream is a seq of signals.
   A signal is the price and weight of a security at a point in time."
  [cash stream]
  (rest (reductions update (portfolio cash (first stream)) stream)))

