(ns overmind.space-watcher
  (:require [com.stuartsierra.component :as component]
            [overmind.path-watcher :as w]
            [clojure.java.shell]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))

(defn get-df
  []
  (-> (clojure.java.shell/sh "df" "-lh")
      :out
      (clojure.string/split #"\n")
      ))


(defn process-dev-line
  [line]
  (->>
    (clojure.string/split line #"\s")
      (remove (fn [s] (clojure.string/blank? s)))
      (zipmap [:dev :size :used :avail :used-perc :mounted])))

(defn dev-space
  [dev]
  (->>
    (get-df)
    rest
    (map process-dev-line)
    (filter #(= dev (:dev %)))
    first
    )
  )

(defn space-event
  [level space]
  {
   :name :space-watcher
   :payload
   {:level level
    :dev (:dev space)
    :datetime (java.util.Date.)
    :used-perc (:used-perc space)
    }}
  )

(defn watch-dev
  [dev watcher-chan]
  (future
    (loop []
      (let [space (-> dev :dev dev-space)
            used (->> space :used-perc (re-find #"\d+") Integer.)
            ]
        (cond
          (> used (:danger-limit dev)) (>!! watcher-chan (space-event "danger" space))
          (> used (:warning-limit dev)) (>!! watcher-chan (space-event "warning" space)))
        (Thread/sleep (* 1000 60 (:frequency dev)))
        (recur)))))

(defn start-watchers
  [devs watcher-chan]
  {
   :watchers (mapv (fn [d]
                     (watch-dev d watcher-chan))
                   devs)
   :watch-chan watcher-chan
   })

(defn stop-watchers
  [watchers]
  (doseq [w watchers]
    (future-cancel w)))

(defrecord SpaceWatcherComponent [devs watcher-chan watchers]
  component/Lifecycle

  (start [component]
    (println ";; Starting Space Watcher")
    (merge component (start-watchers devs watcher-chan)))

  (stop [component]
    (println ";; Stopping Space Watcher" watchers)
    (merge component {:watchers (stop-watchers watchers)})))

;; Constructor
(defn new-space-watcher [devs watcher-chan]
  (map->SpaceWatcherComponent {:devs devs :watcher-chan watcher-chan }))
