(ns kaj.nio.bitmap
  (:import (java.nio.channels SelectionKey)))

(def event-map
  {:read SelectionKey/OP_READ
   :write SelectionKey/OP_WRITE
   :accept SelectionKey/OP_ACCEPT
   :connect SelectionKey/OP_CONNECT
   })

(defmulti to-bitmap type)

(defmethod to-bitmap java.lang.Integer 
  [x] 
  x)

(defmethod to-bitmap clojure.lang.Keyword 
  [x] 
  (to-bitmap (x event-map)))

(defmethod to-bitmap clojure.lang.PersistentHashSet 
  [x] 
  (reduce bit-or (map to-bitmap x)))

(defmethod to-bitmap clojure.lang.PersistentVector 
  [x] 
  (reduce bit-or (map to-bitmap x)))

(defn from-bitmap
  [ops] 
  (into #{} (map #(% 0) (filter #(not (zero? (bit-and ops (% 1)))) event-map))))
