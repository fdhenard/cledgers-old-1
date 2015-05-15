(defproject cledgers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                 [compojure "1.3.4"]
                 [ring/ring-defaults "0.1.2"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]
                 [com.stuartsierra/component "0.2.3"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [reagent "0.5.0"]
                 [com.cognitect/transit-clj "0.8.271"]
                 [com.taoensso/timbre "3.4.0"]]
  :plugins [[lein-ring "0.8.13"]
            [lein-cljsbuild "1.0.6"]]
  :ring {:handler cledgers.handler/app}
  :profiles  {:dev {:source-paths ["dev"]
                    :dependencies [[javax.servlet/servlet-api "2.5"]
                                   [ring-mock "0.1.5"]
                                   [ring/ring-devel "1.3.2"]]}}
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/app-cljs.js"
                                   :optimizations :whitespace
                                   ;;#_:preamble ["reagent/react.js"]
                                   :pretty-print true}}]}
  :hooks [leiningen.cljsbuild]
  :main cledgers.core
  )
