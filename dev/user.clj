(ns user
  (:require [com.stuartsierra.component :as component]
            [reloaded.repl :refer  [system init start stop go reset]]
            [clojure.tools.namespace.repl :refer (refresh)]
            [clojure.edn]
            [overmind.app :as app]))

(comment
"Manual implementation of the reloaded workflow
http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded
"
(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (app/monitoring-system
                                (clojure.edn/read-string (slurp "config.edn"))))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
)

(reloaded.repl/set-init! #(app/monitoring-system
                                (clojure.edn/read-string (slurp "config.edn"))))
