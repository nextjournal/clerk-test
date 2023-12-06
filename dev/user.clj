(ns user
  (:require [nextjournal.clerk :as clerk]
            [clojure.test :as test]
            demo.a-test
            demo.b-test
            demo.c-test
   ;; TODO: fix load order
            [nextjournal.clerk.test :as clerk.test]))

(defn exec-fn [m]
  (clerk/serve! {:port 8888 :browse? true})
  (clerk/show! 'nextjournal.clerk.test)
  #_
  (kaocha.runner/exec-fn {:reporter [clerk.kaocha/report]}))

(comment

  (clerk/serve! {:port 7788})
  (clerk/show! 'nextjournal.clerk.test)
  (clerk.test/reset-state!)

  (binding [test/report clerk.test/report]
    (test/run-tests (the-ns 'demo.a-test)
                    (the-ns 'demo.b-test)
                    (the-ns 'demo.c-test)))

  #_
  (kaocha.repl/run :unit {:reporter [clerk.kaocha/report]}))
