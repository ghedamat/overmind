(ns overmind.fake-out
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.client :as http]
            [overmind.utils :refer [format-event]]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))

;; TODO create logger
(defn- log-error [error]
  (println (str error))
  )

(defn- send-success [response]
  (println (-> response)))

(defn send-message
  [message]
  (println message)
  )

(defn notify-loop
  [from]
  (go-loop [event (<! from)]
           (send-message (format-event event))
           (recur (<! from))
  )
)

(defrecord FakeOutComponent [from-chan]
  component/Lifecycle

  (start [component]
    (println ";; Starting fake out")
    (notify-loop from-chan)
    (merge component {}))

  (stop [component]
    (println ";; Stopping fake out")
    (merge component {})))

;; Constructor
(defn new-fake-out [from-chan]
  (map->FakeOutComponent { :from-chan from-chan }))
