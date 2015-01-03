(ns kaj.node.address
  (:require [kaj.hash :as hash])
  (:import (kaj.hash Hashable)
           (java.net Inet6Address)))

(def ^:dynamic *port* 45678)

(def ^:dynamic *ring* :default)

(defrecord NodeAddress [ring host port]
  Hashable
  (hash-code
    [this]
    (hash/hash-code (str (-> this :ring name)
                         "@" (:host this)
                         ":" (:port this)))))

(def localhost
  (into [] (-> "::1" Inet6Address/getByName .getAddress)))

(defn make
  ([]
   (make *ring*))
  ([ring]
   (make ring localhost))
  ([ring host]
   (make ring host *port*))
  ([ring host port]
   (NodeAddress. ring host port)))
