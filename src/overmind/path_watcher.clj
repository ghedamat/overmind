(ns overmind.path-watcher
  (:require [clojure-watch.core :refer [start-watch]]
            [com.stuartsierra.component :as component]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]
            ))

(defn watch-cbk
  [watcher-chan basepath event filename]
  (when-not (re-find #"\.tmp\Z" filename) ;; Exclude temp files
    (go
      (>! watcher-chan
          {:name :path-watcher
           :payload
           {:event event
            :datetime (java.util.Date.)
            :basepath basepath
            :filename filename}}))))

(defn watch-path
  "watches a single path and executes call back every time a
  file event triggers
  returns a fn that stops the watcher if called"
  ([path cbk]
   (watch-path path cbk nil))
  ([path cbk bcbk]
   (watch-path path cbk bcbk [:create :delete]))
  ([path cbk bcbk watch-opts]
   {:pre [(.exists (clojure.java.io/as-file (:path path)))]}
   (start-watch [{:path (:path path)
                  :event-types watch-opts
                  :bootstrap bcbk
                  :callback cbk
                  :options {:recursive (:recursive path) }}])))

(defn start-watchers
  [paths watch-chan]
  {
   :watchers (mapv (fn [p]
                     (watch-path p (partial watch-cbk watch-chan p)))
                   paths)
   :watch-chan watch-chan
   })

(defn stop-watchers
  [watchers]
  (loop [w (first watchers) wat (rest watchers)]
    (w)
    (if (seq wat)
      (recur (first wat) (rest wat))
      nil)))

(defrecord PathWatcherComponent [paths watcher-chan watchers]
  component/Lifecycle

  (start [component]
    (println ";; Starting Path Watcher")
    (merge component (start-watchers paths watcher-chan)))

  (stop [component]
    (println ";; Stopping Path Watcher" watchers)
    (merge component {:watchers (stop-watchers watchers)})))

;; Constructor
(defn new-path-watcher [paths watcher-chan]
  (map->PathWatcherComponent {:paths paths
                              :watcher-chan watcher-chan
                              }))
