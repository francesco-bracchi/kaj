(ns kaj.messages.find
  (:require [kaj.encoding :as enc]
            [kaj.node :as node])
  (:use kaj.messages))

;; todo (replace 10 with a function of the hash type (:sha-1)
(defmessage Find \F [index corr-id origin])

(defmethod enc/encode-message Find
  [this buf]
  (enc/write-intn 10 (:index this) buf)
  (enc/write-int32 (:corr-id this) buf)
  (enc/write-node (:origin this) buf))

(defmethod enc/decode-message Find
  [buf]
  (Find. (enc/read-intn 10 buf)
         (enc/read-int32 buf)
         (enc/read-node buf)))

(defn random-corr-id
  []
  (rand-int 0xFFFFFFFF))

(defn make
  ([index] 
   (make (random-corr-id)))
  ([index corr-id] 
   (make index corr-id  node/*node*))
  ([index corr-id origin] 
   (Find. index corr-id origin)))
