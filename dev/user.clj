(ns user
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.kaocha :as clerk.kaocha]
            [kaocha.repl]))

(comment
  (clerk/serve! {})
  (clerk/show! 'nextjournal.clerk.kaocha)
  (kaocha.repl/run :unit {:reporter [clerk.kaocha/report]}))
