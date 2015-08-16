(ns overmind.app
  (:require [com.stuartsierra.component :as component]
            [overmind.path-watcher :as w]
            [overmind.notifier :as n]
            [overmind.telegram :as t]
            [overmind.rss :as r]
            [overmind.fake-out :as f]
            [overmind.space-watcher :as s]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))

(def monitoring-system-components [:watcher
                                   :space
                                   :notifier
                                   :telegram
                                   :rss
                                   :fake-out
                                   ])


(defrecord MonitoringSystem [config-options app]
  component/Lifecycle
  (start [this]
    (component/start-system this monitoring-system-components))
  (stop [this]
    (component/stop-system this monitoring-system-components)))

(defn monitoring-system [config-options]
  (let [{:keys [host port]} config-options
        space-chan (chan)
        watcher-chan (chan)
        telegram-chan (chan)
        fake-out-chan (chan)
        rss-chan (chan 1 (filter #(= (:name %) :path-watcher)))
        ]
    (map->MonitoringSystem
      {:config-options config-options
       :watcher (w/new-path-watcher (:paths config-options) watcher-chan)
       :space (s/new-space-watcher (:space-devs config-options) space-chan)
       :notifier (component/using
                   (n/new-notifier [watcher-chan space-chan] [telegram-chan rss-chan fake-out-chan])
                   [:watcher :space])
       :telegram (t/new-telegram telegram-chan (:telegram-chat-id config-options))
       :rss (r/new-rss rss-chan (:rss config-options))
       :fake-out (f/new-fake-out fake-out-chan)
       })))
