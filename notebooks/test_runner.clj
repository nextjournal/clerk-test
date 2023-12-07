(ns test-runner
  {:nextjournal.clerk/visibility {:code :hide :result :hide}
   :nextjournal.clerk/no-cache true}
  (:require [clojure.test :as test]
            [demo.a-test]
            [demo.b-test]
            [demo.c-test]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.test :as clerk.test]))

(binding [test/report clerk.test/report]
  (test/run-all-tests #"demo\..\-test"))

{::clerk/visibility {:code :hide :result :show}}
(clerk/with-viewer clerk.test/test-suite-viewer
  @clerk.test/!test-report-state)

{::clerk/visibility {:code :hide :result :show}}
(comment
  (clerk.test/status-report @clerk.test/!test-report-state)
  (clerk/build! {:paths ["notebooks/test_runner.clj"]})
  )
