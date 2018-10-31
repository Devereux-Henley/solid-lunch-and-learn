(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [fulcro-spec.suite :as suite]
    [fulcro-spec.selectors :as sel]))
;; === SHADOW REPL ===

(comment
  ;; evaluate any one of these in your nREPL to
  ;; choose a (running and connected) shadown-CLJS nREPL
  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :main))

  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :test))

  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :cards)))

; Run (start-server-tests) in a REPL to start a runner that can render results in a browser
(suite/def-test-suite start-server-tests
  {:config       {:port 8888}
   :test-paths   ["src/test"]
   :source-paths ["src/main"]}
  {:available #{:focused :unit :integration}
   :default   #{::sel/none :focused :unit}})
