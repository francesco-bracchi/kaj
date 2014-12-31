(ns kaj.messages
  (:require [kaj.node     :as n]
            [kaj.hash     :as hash]
            [kaj.encoding :as enc]))

(defprotocol Message)

(defmacro defmessage [Name type fields & rest]
  `(do (defrecord ~Name ~fields Message ~@rest)
       (swap! enc/message-id assoc ~type ~Name)))
