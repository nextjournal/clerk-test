(ns user
  (:require [nextjournal.clerk :as clerk]
            demo.a-test
            demo.b-test
            demo.c-test
   ;; TODO: fix load order
            [nextjournal.clerk.kaocha :as clerk.kaocha]))

(defn exec-fn [m]
  (clerk/serve! {:port 8888 :browse? true})
  (clerk/show! 'nextjournal.clerk.kaocha)
  #_
  (kaocha.runner/exec-fn {:reporter [clerk.kaocha/report]}))

(comment
  (get (group-by first (sort (map (comp str ns-name) (all-ns))))
       \d)


  (clerk/serve! {:port 7788})
  (clerk/show! 'nextjournal.clerk.kaocha)
  #_
  (kaocha.repl/run :unit {:reporter [clerk.kaocha/report]}))
