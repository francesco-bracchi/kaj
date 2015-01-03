(ns kaj.messages.ping
  (:refer-clojure :exclude [send])
  (:require [kaj.encoding :as enc]
            [kaj.node :as node]
            [kaj.messages.cid :as cid]
            [kaj.messages.pong :as pong]
            )
  (:use kaj.messages)
  (:import (clojure.lang IFn)))

(defmessage Ping \P [corr-id origin address]
  IFn
  (invoke [this node]
          (when (= address (:address node))
            (send origin (pong/make corr-id address)))
          node))

(defn make
  ([index] 
   (make (cid/make)))
  ([index corr-id] 
   (make index corr-id  node/*node*))
  ([index corr-id origin] 
   (Ping. index corr-id origin)))

(defmethod enc/encode-message Ping
  [this buf]
  (enc/write-int32 (:corr-id this) buf)
  (enc/write-address (:origin this) buf)
  (enc/write-address (:address this) buf))

(defmethod enc/decode-message Ping
  [buf]
  (make (enc/read-int32 buf)
        (enc/read-address buf)
        (enc/read-address buf)))
