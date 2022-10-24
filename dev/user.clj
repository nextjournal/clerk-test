(ns user
  (:require [kaocha.repl]
            [kaocha.runner]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.kaocha :as clerk.kaocha]))

(defn exec-fn [m]
  (clerk/serve! {:port 8888 :browse? true})
  (clerk/show! 'nextjournal.clerk.kaocha)
  (kaocha.runner/exec-fn {:reporter [clerk.kaocha/report]}))

(comment
  (clerk/serve! {})
  (clerk/show! 'nextjournal.clerk.kaocha)
  (kaocha.repl/run :unit {:reporter [clerk.kaocha/report]}))
