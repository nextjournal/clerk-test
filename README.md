# 👩‍🔬 nextjournal.clerk.test

A [Clerk](https://github.com/nextjournal/clerk) test reporter for clojure.test.

## TODO: new video

# Usage

🚧 under construction / REPL driven only ftm

```clojure 
(ns user
  (:require [nextjournal.clerk :as clerk]
            ...require your tests...
            [clojure.test :as test])

(clerk/serve! {:port 7788})

(clerk/show! 'nextjournal.clerk.kaocha)

(binding [test/report clerk.test/report]
  (test/run-tests (the-ns 'test-1)
                  (the-ns 'test-2)))
```
