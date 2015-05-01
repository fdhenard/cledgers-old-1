(defproject cledgers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]
                 [com.stuartsierra/component "0.2.3"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler cledgers.handler/app}
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]]}}
  :main cledgers.core
  )
