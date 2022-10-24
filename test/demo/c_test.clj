(ns demo.c-test
  (:require [clojure.test :refer :all]
            [matcher-combinators.test]))

(deftest one
  (testing "These should"
    (is true)
    (is true)
    (is true)
    (testing "all pass"
      (is true)
      (is true))))

(deftest two
  (testing "These also should"
    (is true)
    (is true)
    (is true)
    (testing "all really pass"
      (is true)
      (is true))))
