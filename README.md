# 🍵 clerk-kaocha

A [Clerk](https://github.com/nextjournal/clerk) test report and utilities [kaocha](https://github.com/lambdaisland/kaocha).

# Usage

🚧 under construction / REPL driven only ftm

```clojure 
(ns user
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.kaocha :as clerk.kaocha]
            [kaocha.repl])

(clerk/serve!)

(clerk/show! 'nextjournal.clerk.kaocha)

(kaocha.repl/run :unit {:reporter [clerk.kaocha/report]})
```
