(ns user
  (:require [clojure.test :as test]
            demo.a-test
            demo.b-test
            demo.c-test
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.test :as clerk.test]))

(defn exec-fn [m]
  (clerk/serve! {:port 8888 :host "localhost" :browse? true})
  (clerk/show! 'nextjournal.clerk.test)
  #_
  (kaocha.runner/exec-fn {:reporter [clerk.kaocha/report]})
  (binding [test/report clerk.test/report]
    (test/run-all-tests #"demo\..\-test")))

(comment

  (clerk/serve! {:port 7788})
  (clerk/show! 'nextjournal.clerk.test)
  (clerk.test/reset-state!)

  (binding [test/report clerk.test/report]
    (test/run-all-tests #"demo\..\-test"))

  ;; used to be a reported for lambdaisland/kaocha {:mvn/version "1.66.1034"}
  #_
  (kaocha.repl/run :unit {:reporter [clerk.kaocha/report]}))
