(ns overmind.telegram
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [org.httpkit.client :as http]
            [overmind.utils :refer [format-event]]
            [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]]
            ))

(def token (:telegram-bot-token env))
(def bot-url (str "https://api.telegram.org/bot" token))
(def send-message-url (str bot-url "/sendMessage"))

;; TODO create logger
(defn- log-error [error]
  (println (str error)))

(defn- send-success [response]
  (println (-> response)))

(defn send-message
  "
  Send the given message to the given chat
  If supplied, execute some functions as success / error callbacks
  "
  ([chat message] (send-message chat message
                                send-success
                                log-error))

  ([chat message success-f error-f]
   (let [options {:form-params {:chat_id chat
                                :text message}}]
     (http/post send-message-url options
                (fn [{:keys [status headers body error]}]
                  (if error
                    (error-f error)
                    (success-f body)))))))

(defn notify-loop
  [from chat-id]
  (go-loop [event (<! from)]
           (send-message chat-id (format-event event))
           (recur (<! from))))

(defrecord TelegramComponent [from-chan chat-id]
  component/Lifecycle

  (start [component]
    (println ";; Starting  telegram")
    (notify-loop from-chan chat-id)
    (merge component {}))

  (stop [component]
    (println ";; Stopping  telegram")
    (merge component {})))

;; Constructor
(defn new-telegram [from-chan chat-id]
  (map->TelegramComponent { :from-chan from-chan :chat-id chat-id }))
