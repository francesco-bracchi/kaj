(ns kaj.nio.datagram
  (:require [kaj.nio.channel :as ch])
  (:import (java.nio.channels DatagramChannel)
           (java.net InetSocketAddress StandardSocketOptions)))

(def ^{:private true} option-map
  {:send-buffer StandardSocketOptions/SO_SNDBUF
   :recv-buffer StandardSocketOptions/SO_RCVBUF
   :reuse-address StandardSocketOptions/SO_REUSEADDR
   :broadcast StandardSocketOptions/SO_BROADCAST
   :type-of-service StandardSocketOptions/IP_TOS
   :multicast-interface StandardSocketOptions/IP_MULTICAST_IF
   :multicast-time-to-live StandardSocketOptions/IP_MULTICAST_TTL
   :multicast-loopback StandardSocketOptions/IP_MULTICAST_LOOP})

(defn- bind [sock-addr]
  (doto (DatagramChannel/open)
    (.bind sock-addr)
    (.configureBlocking false)))

(defn make
  ([port]
   (bind (InetSocketAddress. port)))
  ([host port]
   (bind (InetSocketAddress. host port))))

(defn set-option! 
  ([key val]
   (set-option! key val ch/*channel*))
  ([key val chan]
   (-> chan (.setOption (option-map key) val))))
