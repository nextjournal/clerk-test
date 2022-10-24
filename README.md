# 🍵 clerk-kaocha

A [Clerk](https://github.com/nextjournal/clerk) test report and utilities [kaocha](https://github.com/lambdaisland/kaocha).

https://user-images.githubusercontent.com/1078464/196944993-bcf28cb0-037c-4073-9cf2-837f9b89c690.mp4

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
