;; # 🍵 Kaocha Test Report
(ns nextjournal.clerk.kaocha
  {:nextjournal.clerk/visibility {:code :hide :result :hide}
   :nextjourna.clerk/no-cache true}
  (:require [clojure.test :as t :refer [deftest testing is]]
            [kaocha.report :as r]
            [kaocha.repl]
            [kaocha.history]
            [kaocha.hierarchy]
            [kaocha.config :as config]
            [kaocha.api :as kaocha]
            [lambdaisland.deep-diff :as ddiff]
            [kaocha.matcher-combinators]
            [matcher-combinators.test]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.builder-ui :as builder-ui]
            [nextjournal.clerk.analyzer :as clerk.analyzer]
            [nextjournal.clerk.viewer :as viewer]
            [clojure.string :as str]))

(def initial-state {:test-nss [] :seen-ctxs #{} :summary {}})

(defonce !test-run-events (atom []))
(defonce !test-report-state (atom initial-state))

(defn reset-state! []
  (reset! !test-run-events [])
  (reset! !test-report-state initial-state)
  (clerk/recompute!))

(defn ->test-var-info [{:kaocha.testable/keys [meta] :kaocha.var/keys [name var]}]
  (let [{:kaocha/keys [pending skip]} meta]
    (assoc meta :var var :name name
           :assertions []
           :status (cond pending :pending skip :skip 'else :queued))))

(defn test-plan->test-nss
  "Takes a kaocha test plan, gives a sequence of namespaces"
  [test-plan]
  (map
   (fn [{:kaocha.ns/keys [name ns] :kaocha.test-plan/keys [tests]}]
     {:ns ns
      :name name
      :status :queued
      :file (clerk.analyzer/ns->file ns)
      :test-vars (keep ->test-var-info tests)})
   (-> test-plan :kaocha.test-plan/tests first :kaocha.test-plan/tests)))

(defn ->test-var-name [e] (some-> e :kaocha/testable :kaocha.var/name))
(defn ->test-ns-name  [e]
  (or (some-> e :kaocha/testable :kaocha.ns/name)
      (some-> e :kaocha/testable :kaocha.var/name namespace symbol)))

(defn ->assertion-data
  [{:as event :keys [type] :kaocha/keys [testable] ex :kaocha.result/exception}]
  (let [{:kaocha.var/keys [name var]} testable]
    (-> (select-keys event [:actual :expected :message :file :line])
        (cond-> ex (assoc :exception ex))
        (cond-> (= :kaocha.type.var/zero-assertions type) (assoc :message "This test has no assertions"))
        (assoc :var var :name name
               :status (case type
                         :kaocha.type.var/zero-assertions :fail
                         :kaocha.report/one-arg-eql :fail
                         type)))))

(defn vec-update-if [pred f & args]
  (partial into [] (map (fn [item] (if-not (pred item) item (apply f item args))))))

(defn update-test-ns [state nsn f & args]
  (update state :test-nss (vec-update-if #(= nsn (:name %)) f)))

(defn update-test-var [state varn f]
  (update-test-ns state
                  (-> varn namespace symbol)
                  (fn [test-ns] (update test-ns :test-vars (vec-update-if #(= varn (:name %)) f)))))

(defn update-contexts [{:as state :keys [seen-ctxs]} event]
  (let [ctxs (remove seen-ctxs t/*testing-contexts*)
        depth (count (filter seen-ctxs t/*testing-contexts*))
        ctx-items (map-indexed (fn [i c] {:type :ctx
                                          :ctx/text c
                                          :ctx/depth (+ depth i)}) (reverse ctxs))]
    (-> state
        (update :seen-ctxs into ctxs)
        (update-test-var (->test-var-name event)
                         #(update % :assertions into ctx-items)))))

(kaocha.hierarchy/derive! :pass :assertion)
(kaocha.hierarchy/derive! :fail :assertion)
(kaocha.hierarchy/derive! :error :assertion)
(kaocha.hierarchy/derive! :kaocha.type.var/zero-assertions :assertion)
(kaocha.hierarchy/derive! :kaocha.report/one-arg-eql :assertion)

#_ (ns-unmap *ns* 'build-test-state )
(defmulti build-test-state (fn bts-dispatch [_state {:as _event :keys [type]}] type)
          :hierarchy #'kaocha.hierarchy/hierarchy)

(defmethod build-test-state :default [state event]
  (println :Unhandled (:type event) (->test-ns-name event) (->test-var-name event))
  state)

(defmethod build-test-state :begin-test-suite [_state {:as event :kaocha/keys [test-plan]}]
  (swap! !test-run-events conj event)
  (assoc initial-state :test-nss (test-plan->test-nss test-plan)))

(defmethod build-test-state :begin-test-ns [state event]
  (swap! !test-run-events conj event)
  (update-test-ns state (->test-ns-name event) #(assoc % :status :executing)))

(defmethod build-test-state :begin-test-var [state event]
  (swap! !test-run-events conj event)
  (update-test-var state (->test-var-name event) #(assoc % :status :executing)))

(comment
  (remove-all-methods build-test-state))

(defmethod build-test-state :assertion [state event]
  (swap! !test-run-events conj event)
  (-> state
      (update-contexts event)
      (update-test-var (->test-var-name event) #(update % :assertions conj (->assertion-data event)))))

(defmethod build-test-state :end-test-var [state event]
  (swap! !test-run-events conj event)
  (update-test-var state (->test-var-name event)
                   (fn [{:as test-var :keys [assertions]}]
                     (assoc test-var :status
                            (as-> (map :status assertions) as
                              (or (some #{:error} as) (some #{:fail} as) :pass))))))

(defmethod build-test-state :end-test-ns [state event]
  (swap! !test-run-events conj event)
  (update-test-ns state (->test-ns-name event)
                  (fn [{:as test-ns :keys [test-vars]}]
                    (assoc test-ns :status
                           (as-> (map :status test-vars) ss
                             (or (some #{:error} ss) (some #{:fail} ss) :pass))))))

#_ 'the-kaocha-reporter
#_ (ns-unmap *ns* 'notebook-reporter)
(defn report [{:as event :keys [type]}]
  (swap! !test-report-state
         #(-> %
              (build-test-state event)
              (update :summary
                      (fn [m]
                        (cond
                          (some #{type} [:pass :error :kaocha/pending]) (update m type (fnil inc 0))
                          (kaocha.hierarchy/isa? type :kaocha/begin-test) (update m :test (fnil inc 0))
                          (kaocha.hierarchy/fail-type? event) (update m :fail (fnil inc 0))
                          :else m)))))
  (clerk/recompute!))

(defn bg-class [status]
  (case status
    :pass "bg-green-100"
    :fail "bg-red-50"
    :error "bg-red-50"
    "bg-slate-100"))

(defn status-light [color & [{:keys [size] :or {size 14}}]]
  [:div.rounded-full.border
   {:class (str "bg-" color "-400 border-" color "-700")
    :style {:box-shadow "inset 0 1px 3px rgba(255,255,255,.6)"
            :width size :height size}}])

(defn status->icon [status]
  (case status
    :executing [:div (builder-ui/spinner-svg)]
    :queued (status-light "slate")
    :pending (status-light "amber")
    :skip (status-light "slate")
    :pass (status-light "green")
    :fail (status-light "red")
    :error (builder-ui/error-svg)))

(defn status->text [status]
  (case status
    :queued "Queued"
    :pending "Pending"
    :skip "Skipped"
    :executing "Running"
    :pass "Passed"
    :fail "Failed"
    :error "Errored"))

(defn assertion-badge [{:as ass :keys [status name line expected actual exception message] :ctx/keys [text depth]}]
  (if text
    [:<>
     ;; force contexts to a new line
     [:div.h-0.basis-full]
     [:div.text-slate-500.my-1.mr-2 {:class (when (< 0 depth) (str "pl-" (* 4 depth)))} text]]
    (case status
      :pass [:div.ml-1.my-1 (builder-ui/checkmark-svg {:size 20})]
      :fail [:div.flex.flex-col.p-1.my-2.w-full
             [:div.w-fit
              [:em.text-red-600.font-medium (str name ":" line)]
              (when (and expected actual)
                [:table.not-prose.w-full
                 [:tbody
                  [:tr.hover:bg-red-100.leading-tight.border-none
                   [:td.text-right.font-medium "expected:"]
                   [:td.text-left (viewer/code (pr-str expected))]]
                  [:tr.hover:bg-red-100.leading-tight.border-none
                   [:td.text-right.font-medium "actual:"]
                   [:td.text-left (viewer/code (pr-str actual))]]]])]
             (when message
               [:em.text-orange-500 message])]
      :error [:div.p-1.my-2 {:style {:widht "100%"}}
              [:em.text-red-600.font-medium (str name ":" line)]
              [:div.mt-2.rounded-md.shadow-lg.border.border-gray-300.overflow-scroll
               {:style {:height "200px"}} (viewer/present exception)]])))

(defn test-var-badge [{:keys [name status line assertions]}]
  (let [collapsible? (seq assertions)]
    [(if collapsible? :details :div)
     (cond-> {:class (str "mb-2 rounded-md border border-slate-300 px-3 py-2 font-sans shadow " (bg-class status))}
       (and collapsible? (= status :executing))
       (assoc :open true))
     [(if collapsible? :summary.cursor-pointer :div.pl-3)
      [:span.mr-2.inline-block (status->icon status)]
      [:span.inline-block.text-sm.mr-1 (status->text status)]
      [:span.inline-block.text-sm.font-medium.leading-none (str name ":" line)]]
     (when (seq assertions)
       (into [:div.flex.flex-wrap.mt-2.py-2.border-t-2] (map assertion-badge) assertions))]))

(defn test-ns-badge [{:keys [status file test-vars]}]
  [:details.mb-2.rounded-md.border.border-slate-300.font-sans.shadow.px-4.py-3
   (cond-> {:class (bg-class status)} (= status :executing) (assoc :open true))
   [:summary
    [:span.mr-2.inline-block (status->icon status)]
    [:span.text-sm.mr-1 (status->text status)]
    [:span.text-sm.font-semibold.leading-none.truncate file]]
   (into [:div.ml-2.mt-2] (map test-var-badge) test-vars)])

(def test-ns-viewer {:transform-fn (viewer/update-val (comp viewer/html test-ns-badge))})

(def test-suite-viewer
  {:transform-fn (comp viewer/mark-preserve-keys
                       (viewer/update-val
                        (fn [state]
                          (-> state
                              (update :test-nss (partial map (partial viewer/with-viewer test-ns-viewer)))
                              (update :summary #(when (seq %)
                                                  (clerk/with-viewer clerk/table
                                                                     (into [] (map (juxt (comp str/capitalize name first) second)) %))))))))
   :render-fn '(fn [{:keys [test-nss summary]} opts]
                 [:div
                  (when (:nextjournal/value summary)
                    [nextjournal.clerk.render/inspect summary])
                  (if-some [xs (seq (:nextjournal/value test-nss))]
                    (into [:div.flex.flex-col.pt-2] (nextjournal.clerk.render/inspect-children opts) xs)
                    [:h5 [:em.slate-100 "Waiting for tests to run..."]])])})


;; Eval the commented form to get started (FIXME: can't it be part of notebook?)
{::clerk/visibility {:code :show :result :hide}}
(comment
  (kaocha.repl/run :unit {:reporter [report]
                          :kaocha.plugin.randomize/randomize? false}))

{::clerk/visibility {:code :hide :result :show}}
(clerk/with-viewer test-suite-viewer
                   @!test-report-state)

{::clerk/visibility {:code :hide :result :hide}}
(comment
  (def cfg
    {:reporter [report]
     :capture-output? false
     :color? true
     :randomize? false})

  (kaocha.repl/run :unit)
  (reset-state!)

  (kaocha.repl/config)
  (kaocha.repl/config cfg)

  (test-plan->test-nss (kaocha.repl/test-plan))
  (test-plan->test-nss (kaocha.repl/test-plan cfg))

  (map :type  @!test-run-events)
  (count  @!test-run-events)

  (do
    (reset! !test-report-state
            (reduce build-test-state {}
                    (take 5 @!test-run-events)))
    (clerk/recompute!))

  @!test-report-state

  ;; inspect events
  (defn get-event [type]
    (some #(when (= type (:type %)) %)
          @!test-run-events))
  (-> (get-event :error)  )

  (nextjournal.clerk/clear-cache!)
  (clerk/serve! {:port 7788})
  (clerk/show! 'nextjournal.clerk.kaocha)
  )
