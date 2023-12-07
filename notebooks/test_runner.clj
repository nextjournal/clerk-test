(ns test-runner
  {:nextjournal.clerk/visibility {:code :hide :result :hide}
   :nextjourna.clerk/no-cache true}
  (:require [clojure.test :as test]
            [demo.a-test]
            [demo.b-test]
            [demo.c-test]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.test :as clerk.test]))

(defn flatten-state [plan]
  (keep :name (tree-seq map? (some-fn :test-nss :test-vars) plan)))

(prn :test-paths (clerk.test/test-plan))

(binding [test/report clerk.test/report]
  (test/run-all-tests #"demo\..\-test"))

(prn :end-tests (flatten-state @clerk.test/!test-report-state))

(Thread/sleep 1000)

{::clerk/visibility {:code :hide :result :show}}
(clerk/with-viewer clerk.test/test-suite-viewer
  @clerk.test/!test-report-state)

(prn :done)


(comment

  (clerk/build! {:paths ["notebooks/test_runner.clj"]})

  )
