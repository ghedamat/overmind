(ns overmind.core
   (:gen-class)
   (:require [com.stuartsierra.component :as component]
             [overmind.app :as app]
             [clojure.edn]
             )
  )

(defn -main
  [& args]
  (let [system (component/start (app/monitoring-system (clojure.edn/read-string (slurp "config.edn"))))]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread.  (fn  []
                                  (println "stop")
                                  (component/stop system))))
    ))
