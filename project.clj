(defproject solid "0.1.0-SNAPSHOT"
  :description "My Cool Project"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [thheller/shadow-cljs "2.4.22"]
                 [fulcrologic/fulcro "2.6.0-RC9"]

                 ; only required if you want to use this for tests
                 [fulcrologic/fulcro-spec "2.1.0-1" :scope "test" :exclusions [fulcrologic/fulcro]]]

  :source-paths ["src/main"]
  :test-paths ["src/test"]

  :test-refresh {:report       fulcro-spec.reporters.terminal/fulcro-report
                 :with-repl    true
                 :changes-only true}

  :profiles {:cljs       {:source-paths ["src/main" "src/test" "src/cards"]
                          :dependencies [[binaryage/devtools "0.9.10"]
                                         [org.clojure/core.async "0.4.474"]
                                         [fulcrologic/fulcro-inspect "2.2.1" :exclusions [fulcrologic/fulcro-css]]
                                         [devcards "0.2.4" :exclusions [cljsjs/react cljsjs/react-dom]]]}})
