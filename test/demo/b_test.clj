(ns demo.b-test
  (:require [clojure.test :refer :all]))

(def exhibits ["🦖" "🦕"])
(def a-stegosaurus? #{'a-stego})
(def a-trex? #{"🦖"})

(deftest any-exhibits-test
  (testing "When I visit a museum, I expect to see something"
    (is (not-empty exhibits))))

(deftest what-for-exhibits-test
  (testing "When I visit a museum"
    (testing "I should be seeing a T-Rex"
      (is (a-trex? (first exhibits))))
    (testing "I should be seeing a Stegosaurus"
      (is (a-stegosaurus? (second exhibits))))))

(deftest a-lost-world
  (testing "When I visit a museum, exhibits shouldn't be alive"
    (throw (Error. "Roar!"))))
