(defproject overmind "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clojure-watch "LATEST"]
                 [com.stuartsierra/component "0.2.3"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [http-kit "2.1.18"]
                 [clj-rss "0.2.1"]
                 [environ "1.0.0"]
                 ]
  :profiles {:dev {
                   :dependencies [[midje "1.5.1"]
                                  [reloaded.repl "0.1.0"]]
                   :source-paths  ["dev"]
                   :main overmind.core
                   }
             :repl {:main user}
             :uberjar {
                       :aot :all
                       :main overmind.core
                       }
             })
