(ns kaj.nio.buffer
  (:require [kaj.nio.channel :as chan])
  (:import (java.nio ByteBuffer)))

(def ^:dynamic *buffer-size* (* 1024 1024))

(def ^:dynamic *buffer* nil)

(defn make
  ([] (make *buffer-size*))
  ([size] (ByteBuffer/allocate size)))

(defmacro with-buffer 
  [[expr] & body]
  `(binding [*buffer* ~expr] ~@body))
     
(defn flip!
  ([] (flip! *buffer*))
  ([buf] (.flip buf)))

(defn clear!
  ([] (clear! *buffer*))
  ([buf] (.clear buf)))

(defn read!
  ([] (read! *buffer*))
  ([buf] (read! buf chan/*channel*))
  ([buf chan] (.receive chan buf)))
