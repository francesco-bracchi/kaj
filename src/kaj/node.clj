(ns kaj.node
  (:require [kaj.hash         :as hash]
            [kaj.node.address :as addr]
            [taoensso.timbre  :as log])
  (:import (kaj.hash     Hashable)
           (clojure.lang IFn)
           (java.net Inet6Address)))

(def ^:dynamic *node* nil)

(def ^:dynamic *limit* 10)

(defrecord Node [address finger-table handlers]
  Hashable
  (hash-code
    [this]
    (hash/hash-code (:address this))))

(defn make
  ([] 
   (make (addr/make)))
  ([address] 
   (make address {}))
  ([address finger-table]
   (make address finger-table {}))
  ([address finger-table handlers]
   (Node. address finger-table handlers)))

(defn atomic
  [addr]
  (atom (make addr)))

(defn index
  ([val] (index val *node*))
  ([val node] 
   (.bitLength (hash/distance val (:address node)))))

(defn make-rooms
  [bucket]
  (if (>= (count vec) *limit*)
    (into [] (drop 1 bucket))
    bucket))

(defn closer
  [p a b]
  (<= (hash/distance p a) (hash/distance p b)))

(defn closest-nodes 
  ([num val] 
   (closest-nodes num val *node*))
  ([num val node]
   (let [table (:finger-table node)
         index (index val node)]
     (take num (sort-by (partial closer val) (table index))))))

(defn into-bucket 
  [addr bucket]
  (conj (make-rooms bucket) addr))

(defn into-table
  [table index addr]
  (assoc table index (into-bucket addr (table index))))

(defn aconj
  [node addr]
  (assoc node
         :finger-table 
         (into-table (:finger-table node) 
                     (index addr node)
                     addr)))

(defmacro with-node [[expr] & body]
  `(binding [*node* ~expr]
     ~@body))
