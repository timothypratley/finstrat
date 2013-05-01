(ns finstrat.portfolio)

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
        s (signal :symbol)
        _ (assert (pos? price)
                  "Should not buy free securities")
        fee (get-in p [:fees :trade])
        units (int (/ (- spend fee) price))
        _ (assert (pos? units)
                  "Should buy more than zero units")
        spend (+ (* units price) fee)]
    (assert (>= (p :cash) spend)
            "Should not spend more than available")
    (-> p
        (update-in [:cash] - spend)
        (update-in [:comments] (fnil conj [])
                   (str "bought " units " " s
                        " @ " price " for $" spend))
        (update-in [:security s]
                   #(-> %
                        (update-in [:units] + units)
                        (update-in [:cost] + spend))))))

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

(defn- raw-value
  ([p signal units]
   (* units (signal :price)))
  ([p signal]
   (raw-value p signal
              (get-in p [:security (signal :symbol) :units]))))

(defn- sale-value
  "Tax adjusted current value of a security as of signal in a portfolio p"
  ([p signal units]
   (if (pos? units)
     (- (raw-value p signal units)
        (tax p signal units)
        (get-in p [:fees :trade]))
     0))
  ([p signal]
   (sale-value p signal
               (get-in p [:security (signal :symbol) :units]))))

(defn- total
  [p signals valuation]
  (apply + (p :cash) (map (partial valuation p) signals)))

(defn- sell-target
  [p signal value]
  (assert (pos? value)
          "Should only sell positive values")
  (assert (pos? (signal :price))
          "Should only sell when there is a price")
  (int (/ value (signal :price))))

; TODO: tax is only paid in April if not withheld
(defn- sell
  ([p signal]
   (sell p signal
         (get-in p [:security (signal :symbol) :units])))
  ([p signal units]
   (assert (pos? units)
           "Should only sell positive units")
   (let [s (signal :symbol)
         held (get-in p [:security s :units])
         fee (get-in p [:fees :trade])
         _ (assert (pos? units)
                   "Should sell at least one unit")
         _ (assert (and (<= units held) (pos? held))
                   "Should only sell securities held in the portfolio")
         proceeds (sale-value p signal units)
         _ (assert (pos? proceeds)
                   "Cost should not exceed sale")]
     (-> p
         (update-in [:cash] + proceeds)
         (update-in [:comments] (fnil conj [])
                    (str "sold " units " " s
                         " @ " (signal :price) " for $" proceeds))
         (update-in [:security s]
                    #(-> %
                         (update-in [:units] - units)
                         (update-in [:cost] - (cost-of % units))))))))

(defn- ballance
  "Given a portfolio that is only invested in signals,
  evaluate how many securities should be bought or sold
  to maintain a relatively equal weighting between them."
  [p signals]
  (let [raw-total (total p signals raw-value)
        target (/ raw-total (count signals))
        ; only buy and sell if a long way off the target allocation
        tolerance 0.2
        gap (fn [signal]
              (- target (raw-value p signal)))
        gap-proportion (fn [signal]
                         (/ (gap signal) target))
        sell? (fn [signal]
                (and
                 (< (gap-proportion signal) (- tolerance))
                 (pos? (sale-value p signal
                                   (sell-target p signal (- (gap signal)))))))
        sell-list (filter sell? signals)
        trim (fn [p signal]
               (sell p signal
                     (sell-target p signal (- (gap signal)))))
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
        best-weight ((last sorted) :weight)
        ; http://www.investopedia.com/terms/l/long.asp
        long? #(>= (% :weight) best-weight 0.1)
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
   :fees {:trade 9}
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
                           (assoc :value (sale-value p signal)))))))))

(defn- update
  [p signals]
  ; TODO: nicer way to remap "Adj Close" to :price, "Date" to :date
  ; or do it in data? (price might be a function of bid/ask/open/close)
  (let [p (dissoc p :comments)
        p (reweight p signals)
        p (reduce update-security p signals)
        date ((first signals) :date)]
    ;; invariant - TODO: how to do invariant in Clojure (entry, during, exit)
    (assert (not (neg? (p :cash)))
            "Cash should not be overdrawn")
    ; TODO: should this be handled differently?
    (assert (every? #(= date (% :date)) (rest signals))
            (apply str "Signals should all have the same date" (map :date signals)))
    (assoc p
      :date date
      :value (total p signals sale-value))))

(defn evaluate
  "Evaluate the performance of a portfolio over historical data.
  stream is a seq of signals.
  A signal is the price and weight of a security at a point in time."
  [cash stream]
  (rest (reductions update (portfolio cash (first stream)) stream)))

