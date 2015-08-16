(ns overmind.rss
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [org.httpkit.client :as http]
            [clj-rss.core :as rss]
            [overmind.utils :refer [format-event]]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))


(defn write-feed
  [event rss-conf]
  (let [{feed-file :feed-file
         feed-storage :feed-storage
         feed-url :feed-url
         feed-title :feed-title} rss-conf
        old-feed (-> feed-storage slurp read-string)
        title (format-event event)
        new-feed (conj
                   old-feed
                   {:title title :link feed-url :description ""})
        ]
    (spit feed-storage (pr-str new-feed))
    (->>
      (rss/channel-xml
        {:title feed-title :link feed-url :description ""}
        new-feed)
      (spit feed-file))))

(defn notify-loop
  [from rss-conf]
  (go-loop [event (<! from)]
           (write-feed event rss-conf)
           (recur (<! from))))

(defrecord RssComponent [from-chan rss-conf]
  component/Lifecycle

  (start [component]
    (println ";; Starting  telegram")
    (notify-loop from-chan rss-conf)
    (merge component {}))

  (stop [component]
    (println ";; Stopping  telegram")
    (merge component {})))

;; Constructor
(defn new-rss [from-chan rss-conf]
  (map->RssComponent {:from-chan from-chan :rss-conf rss-conf }))
