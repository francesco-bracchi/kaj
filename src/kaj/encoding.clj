(ns kaj.encoding
  (:refer-clojure :exclude [read-string])
  (:require [kaj.nio.buffer :as buf]
            [kaj.hash       :as hash]
            [kaj.node       :as node])
  (:import (java.net Inet6Address)))

(defn write-char
  ([c]
   (write-char c buf/*buffer*))
  ([c buf]
   (.putChar buf c)))

(defn read-char 
  ([] 
   (read-char buf/*buffer*))
  ([buf]
   (.getChar buf)))
  
(defn write-bytes
  ([bytes] (write-bytes bytes buf/*buffer*))
  ([bytes buf]
   (if (empty? bytes) 'ok
       (do (.put buf (byte (first bytes)))
           (recur (rest bytes) buf)))))

(defn read-bytes 
  ([n] (read-bytes n buf/*buffer*))
  ([n buf] 
   (let [b (byte-array n)]
     (.get buf b)
     b)))

(defn write-intn
  ([n val]
   (write-intn n val buf/*buffer*))
  ([n val buffer]
   (write-bytes 
    (map #(bit-and (bit-shift-right val %) 0xFF)
         (reverse (map #(* 8 %) (range n)))))))

(defn read-intn
  ([n] 
   (read-intn n buf/*buffer*))
  ([n buf]
   (reduce #(+ %2 (* %1 0xFF)) (read-bytes n))))

(def write-int16 (partial write-intn 2))

(def read-int16 (partial read-intn 2))

(def write-int24 (partial write-intn 3))

(def read-int24 (partial read-intn 3))

(def write-int32 (partial write-intn 4))

(def write-index (partial write-intn 10))

(def read-int32 (partial read-intn 4))

(def read-index (partial write-intn 10))

(defn write-string 
  ([s] 
   (write-string s buf/*buffer*))
  ([s buf]
   (let [raw (.getBytes s "UTF-8")
         len (count raw)]
     (write-int16 len buf)
     (.put raw))))

(defn read-string
  ([] 
   (read-string buf/*buffer*))
  ([buf]
   (let [len (read-int16 buf)
         raw (byte-array len)]
     (.get buf raw 0 len))))

(defn write-address
  ([addr] 
   (write-address addr buf/*buffer*))
  ([addr buf]
   (write-intn 16 (.getAddress (:host addr)) buf)
   (write-int16 (:port addr))
   (write-string (name (:ring addr)))))

(defn read-address
  ([] (read-address buf/*buffer*))
  ([buf]
   (node/make (Inet6Address/getByAddress (read-intn 16 buf))
              (read-int16 buf)
              (keyword (read-string buf)))))

(def message-id (atom {}))

(defmulti encode-message type)

(defmulti decode-message #(@message-id %))

(defn write-length! [buf]
  (let [p (- (.position buf) 4)]
    (assert (< p 0xFFFFFF))
    (.position 1)
    (write-int24 p)))

(defn write-message
  ([type body]
   (write-message type body buf/*buffer*))
  ([type body buf]
   (write-char type buf)
   (.position buf 4)
   (encode-message body)
   (write-length! buf)))

(defn encode
  ([msg]
   (encode msg buf/*buffer*))
  ([msg buf]
   (write-message (@type msg) msg buf)))

(defn decode
  ([] 
   (decode buf/*buffer*))
  ([buf] 
   (let [t (read-char buf)
         l (read-int24 buf)]
     (.limit buf (+ l 4))
     (decode-message t buf))))


;; (defmessage Find \F [origin super])

