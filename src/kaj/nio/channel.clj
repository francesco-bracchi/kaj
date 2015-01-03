(ns kaj.nio.channel
  (:refer-clojure :exclude [send])
  (:require [kaj.nio.bitmap :as bmp]
            [kaj.nio.selector :as sel])
  (:import (java.net InetSocketAddress)))

(defn call-with-open-channel [ch fun]
  (try (fun ch)
       (finally (when (.isOpen ch) (.close ch)))))

(defmacro with-open-channel [[ch expr] & body]
  `(call-with-open-channel ~expr (fn [~ch] ~@body)))

(def ^:dynamic *channel* nil)

(defmacro with-channel [[expr] & body]
  `(with-open-channel [ch# ~expr]
     (binding [*channel* ch#]
       ~@body)))  

(def ^:dynamic *events* #{:accept :read})

(defn register 
  ([callback] 
   (register callback *events*))
  ([callback events] 
   (register callback events *channel*))
  ([callback events chan] 
   (register callback events chan sel/*selector*))
  ([callback events chan selector]
   (.register chan selector (bmp/to-bitmap events) callback)))

(defn send 
  ([buf addr]
   (send buf addr *channel*))
  ([buf addr chan]
   (.send chan buf (InetSocketAddress. (:host addr) (:port addr)))))
