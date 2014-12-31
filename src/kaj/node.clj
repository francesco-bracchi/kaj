(ns kaj.node
  (:require [kaj.hash        :as hash]
            [taoensso.timbre :as log])
  (:import (kaj.hash     Hashable)
           (clojure.lang IFn)
           (java.net Inet6Address)))

(def ^:dynamic *node* nil)

(def ^:dynamic *port* 45678)

(defrecord Node [host port]
  Hashable
  (hash-code
    [this]
    (let [key (str (:host this) ":" (:port this))]
      (hash/hash-code key))))

(defn make
  ([] (make (Inet6Address/getByName "::1")))
  ([host] (make host *port*))
  ([host port] (Node. host port)))

(defn port 
  ([] (port *node*))
  ([n] (:port n)))

(defn host 
  ([] (host *node*))
  ([n] (:host n)))

(def ^:dynamic *limit* 10)

(def ^:dynamic *state* {})

;; **todo** : memoize hash-code
(defn distance
  ([val] (distance val *node*))
  ([v0 v1]
   (bit-xor (hash/hash-code v0)
            (hash/hash-code v1))))

(defn finger-key
  [val]
  (.bitLength (distance val)))

(defn bucket
  [val]
  (*state* (finger-key val)))

(defn make-rooms
  [bucket]
  (if (>= (count vec) *limit*)
    (into [] (drop 1 bucket))
    bucket))

(defn add-to-bucket
  [bucket node]
  (conj (make-rooms bucket) node))

(defn closer
  [p a b]
  (<= (distance p a) (distance p b)))

(defn closest-nodes 
  [n val]
  (take n (sort-by (partial closer val) (bucket val))))

(defn insert! [node]
  (let [key (finger-key node)
        buck (*state* key)]
    (set! *state* 
          (assoc *state*
                 key 
                 (add-to-bucket buck node)))))

(defmacro with-node [[expr] & body]
  `(binding [*node* ~expr
             *state* {}] 
     ~@body))
