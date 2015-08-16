(ns overmind.notifier
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))

(defn send-notification
  "receives a notification in the form of
  {:event str :basepath str filename str}
  and an array of callbacks to call
  "
  [event tos]
  (doseq [to tos]
    (go (>! to event))))

(defn notify-loop
  [froms tos]
  (go-loop []
           (if-let [[v c] (alts! froms)]
             (send-notification v tos))
           (recur)))

(defrecord NotifierComponent [from-chans to-chans]
  component/Lifecycle

  (start [component]
    (println ";; Starting  Notifier")
    (notify-loop from-chans to-chans)
    (merge component {}))

  (stop [component]
    (println ";; Stopping  Notifier")
    (merge component {})))

;; Constructor
(defn new-notifier [from-chans to-chans]
  (map->NotifierComponent {
                           :from-chans from-chans
                           :to-chans to-chans
                              }))
