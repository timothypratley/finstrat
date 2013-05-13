(ns finstrat.portfolio)

;; TODO: use dataflow for :values as they are dependent
;; What are the implications of floating point precision?
;; Buying at close is impractical, but no less possible than at open.
;; Modelling intra day activity has challenges (data and rules).
;; Using close to open is possible but introduces an unnecessary
;; discontinuity.

(defn- buy-target
  [p signal value]
  (assert (pos? value)
          "Should only buy positive values")
  (assert (pos? (signal :price))
          "Should only buy when there is a price")
  (int (/ (- value (get-in p [:fees :trade])) (signal :price))))

(defn- buy
  [p signal units]
  (assert (pos? units)
          "Should only buy positive units")
  (let [price (signal :price)
        sym (signal :symbol)
        _ (assert (pos? price)
                  "Should not buy free securities")
        _ (assert (pos? units)
                  "Should buy more than zero units")
        spend (+ (* units price) (get-in p [:fees :trade]))]
    (assert (>= (p :cash) spend)
            "Should not spend more than available")
    (-> p
        (update-in [:cash] - spend)
        (update-in [:comments] (fnil conj [])
                   (str "bought " units " " sym
                        " @ " price " for $" spend))
        (update-in [:security sym]
                   #(-> %
                        (update-in [:units] (fnil + 0) units)
                        (update-in [:cost] (fnil + 0) spend))))))

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
    (* profit (p :tax-rate))))

(defn- raw-value
  ([p signal units]
   (* units (get signal :price 0)))
  ([p signal]
   (raw-value p signal
              (get-in p [:security (signal :symbol) :units] 0))))

(defn- sale-value
  "Tax adjusted current value of a security as of signal in a portfolio p"
  ([p signal units]
   (if (and units (pos? units))
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
  (let [target (/ (p :value) (count signals))
        ; only buy and sell if a long way off the target allocation
        tolerance 0.2
        gap (fn [signal]
              (- target (sale-value p signal)))
        gap-proportion (fn [signal]
                         (/ (gap signal) target))
        ;; TODO: simplify the conditionals
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
                 (let [units (buy-target p signal
                                          (min (p :cash) (gap signal)))]
                   (if (pos? units)
                     (buy p signal units)
                     p)))
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
        long? (fn [sig]
                (>= (sig :weight) best-weight 0.1))
        long-sigs (take-while long? (reverse sorted))
        ; TODO: try ballancing into only the newest/mid/oldest outperforms
        ; http://www.investopedia.com/terms/s/short.asp
        ;worst-weight ((first sorted) :weight)
        ;short? (fn [sig]
        ;         (<= (sig :weight) worst-weight 0.1)
        ;short-sigs (take-while short? sorted)
        sells (remove (set long-sigs) signals)
        sells (filter (fn [sig]
                        (pos? (get-in p [:security (sig :symbol) :units] 0)))
                      sells)
        p (reduce sell p sells)
        p (if (empty? long-sigs)
            p
            (ballance p long-sigs))]
    p))

(defn portfolio
  "Create a portfolio"
  [cash]
  {:cash cash
   :tax-rate 0.2
   :fees {:trade 9}
   :value cash})

(defn- update-security-value
  [p signal]
  (update-in p [:security (signal :symbol)]
             (fn [security]
               (-> security
                   (assoc :price (signal :price))
                   (assoc :value (sale-value p signal))))))

(defn- update
  [p [date signals]]
  ;; TODO: destructure vals or don't use :symbol, or something else
  (let [signals (vals signals)
        p (dissoc p :comments)
        p (reweight p signals)
        p (reduce update-security-value p signals)
        dates (map :date signals)]
    ;; invariant - TODO: how to do invariant in Clojure (entry, during, exit)
    (assert (not (neg? (p :cash)))
            "Cash should not be overdrawn")
    ; TODO: or should I just use the max?
    (assert (apply = date dates)
            "Signals should all have the same date")
    (assoc p
      :date (first dates)
      :value (total p signals sale-value))))

(defn evaluate
  "Evaluate the performance of a portfolio over historical data.
  stream is a seq of signals.
  A signal is the price and weight of a security at a point in time."
  [p stream]
          ; TODO sim seems to have worked
          ; cash changes value, but not shown in graph...
  (rest (reductions update p stream)))
