(ns kaj.messages.pong
  (:refer-clojure :exclude [send])
  (:require [kaj.encoding :as enc]
            [kaj.node :as node]
            [kaj.messages.cid :as cid])
  (:use kaj.messages)
  (:import (clojure.lang IFn)))

(defmessage Pong \p [corr-id origin]
  IFn
  (invoke [this node] 
          node))

(defn make
  ([index] 
   (make (cid/make)))
  ([index corr-id] 
   (make index corr-id  (:address node/*node*))))

(defmethod enc/encode-message Pong
  [this buf]
  (enc/write-int32 (:corr-id this) buf)
  (enc/write-address (:origin this) buf))

(defmethod enc/decode-message Pong
  [buf]
  (make (enc/read-int32 buf)
        (enc/read-address buf)))
