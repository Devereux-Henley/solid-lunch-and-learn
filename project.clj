(defproject solid "0.1.0-SNAPSHOT"
  :description "My Cool Project"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure     "1.9.0"]
                 [org.clojure/core.async  "0.4.474"]
                 [thheller/shadow-cljs    "2.7.3"]
                 [fulcrologic/fulcro      "2.6.15"]
                 [funcool/cats            "2.3.1"]
                 [funcool/promesa         "1.9.0"]
                 [com.wsscode/pathom      "2.2.0-beta14"]

                 ; only required if you want to use this for tests
                 [fulcrologic/fulcro-spec "2.1.3" :scope "test" :exclusions [fulcrologic/fulcro]]]

  :source-paths ["src/main"]
  :test-paths ["src/test"]
  :plugins [[lein-ancient "0.6.15"]]

  :test-refresh {:report       fulcro-spec.reporters.terminal/fulcro-report
                 :with-repl    true
                 :changes-only true}

  :profiles {:cljs       {:source-paths ["src/main" "src/test" "src/cards"]
                          :dependencies [[binaryage/devtools         "0.9.10"]
                                         [org.clojure/core.async     "0.4.474"]
                                         [fulcrologic/fulcro-inspect "2.2.4" :exclusions [fulcrologic/fulcro-css]]
                                         [devcards                   "0.2.6" :exclusions [cljsjs/react cljsjs/react-dom]]]
                          }})
