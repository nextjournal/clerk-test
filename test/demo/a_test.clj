(ns demo.a-test
  (:require [clojure.test :refer :all]
            [matcher-combinators.test]))

(deftest no-assertions-test
  true)

(deftest one-arg-eq-test
  (is (= 1) 2))

(deftest matcher-combo-test
  (is (match? {:a [1 2 3]}
              {:a [1 2]})))

(deftest some-test
  (is true)
  (is true)
  (testing "When I fight against flex"
    (is true)
    (testing "and I want to force break a line"
      (is true)
      (is true)
      (testing "and I set flex-basis..."
        (is true) (is true) (is true) (is true) (is true)))))

(deftest ^:skip all-passing
  (testing "let them all pass"
    (is true)
    (is true)
    (is true)
    (is true)
    (is true)))

(deftest ^:pending some-other-test
  (is true) (is true)
  (testing "should fail somewhere"
    (is true)
    (is true)
    (is (= 1 2))
    (is true)
    (is (= :a :b))
    (is true)
    (is true)))
