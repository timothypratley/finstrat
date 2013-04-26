(ns finstrat.test.portfolio_tests
  (:use [finstrat.portfolio]
        [clojure.test]))

(deftest test-buy
  (let [p {:cash 20
           :fees {:trade 0}
           :security {"X" {:units 0
                           :cost 0}}}
        signal {:symbol "X"
                :price 10}]
    (testing "Simple"
      (let [p (#'finstrat.portfolio/buy p signal 10)
            units (get-in p [:security "X" :units])]
        (is (= units 1))
        (is (= (p :cash) 10))))
    (testing "Fees"
      (let [p (assoc-in p [:fees :trade] 9)
            p (#'finstrat.portfolio/buy p signal 20)
            units (get-in p [:security "X" :units])]
        (is (= units 1))
        (is (= (p :cash) 1))))))

(deftest test-sell
  (let [p {:cash 0
           :tax 0
           :fees {:trade 0}
           :security {"X" {:units 1
                           :cost 10}}}
        signal {:symbol "X"
                :price 11}]
    (testing "Simple"
      (let [p (#'finstrat.portfolio/sell p signal 11)]
        (is (= (p :cash) 11))))
    (testing "Taxed"
      (let [p (assoc p :tax 0.2)
            p (#'finstrat.portfolio/sell p signal)]
        (is (= (p :cash) 10.8))))
    (testing "Fee"
      (let [p (assoc-in p [:fees :trade] 9)
            p (#'finstrat.portfolio/sell p signal)]
        (is (= (p :cash) 2))))))

(deftest test-tax
  (let [p {:tax 0.2
           :security {"X" {:units 1
                           :cost 10}}}
        signal {:symbol "X"
                :price 11}]
    (is (= (#'finstrat.portfolio/tax p signal 1) 0.2))))
