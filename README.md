# 👩‍🔬 nextjournal.clerk.test

A [Clerk](https://github.com/nextjournal/clerk) test reporter for clojure.test.

## TODO: new video

# Usage

🚧 under construction / REPL driven only ftm

```clojure 
(ns user
  (:require ...your tests...
            [clojure.test :as test]
            [nextjournal.clerk :as clerk])

(clerk/serve! {:port 7788})

(clerk/show! 'nextjournal.clerk.test)

(binding [test/report clerk.test/report]
  (test/run-tests (the-ns 'test-1)
                  (the-ns 'test-2)))
```
