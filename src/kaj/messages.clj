(ns kaj.messagesxo
  (:refer-clojure :exclude [send])
  (:require [kaj.node         :as n]
            [kaj.hash         :as hash]
            [kaj.nio.buffer   :as buf]
            [kaj.nio.channel  :as chan]
            [kaj.nio.datagram :as datagram]
            [kaj.encoding     :as enc]))

(defprotocol Message)

(defmacro defmessage [Name type fields & rest]
  `(do (defrecord ~Name ~fields Message ~@rest)
       (assert (nil? (@enc/message-id ~type)))
       (swap! enc/message-id assoc ~type ~Name)))

(defn send 
  [dest msg]
  (buf/with-buffer [(buf/make)] 
    (chan/with-channel [(datagram/make)]
      (buf/clear!)
      (enc/encode msg)
      (buf/flip!)
      (chan/send buf/*buffer* dest))))


