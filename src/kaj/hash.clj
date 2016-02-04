(ns kaj.hash
  (:import (clojure.lang IFn)
           (java.security MessageDigest)
           (java.util UUID)))

(defn random-salt [] (str (UUID/randomUUID)))

(defn hash-string [type ^String salt ^String input]
  (let [^MessageDigest digest (-> type name .toUpperCase MessageDigest/getInstance)]
    (doto digest
      .reset
      (.update (.getBytes salt "UTF-8"))
      (.update (.getBytes input "UTF-8")))
    (-> digest .digest biginteger)))

;; ## Hash 
(defrecord Hash [salt type]
  IFn 
  (invoke
    [this input]
    (hash-string (:type this) 
                 (:salt this) 
                 input)))

(defn make
  ([] (make (random-salt)))
  ([salt] (make salt :sha-256))
  ([salt type] (Hash. (str salt) (keyword type))))

(def ^:dynamic *hash* (make *ns*))

(defprotocol Hashable
  "something that can stay on the kademlia space (a key or a node)"
  (-hash-code [this hash]))

(extend-protocol Hashable
  Object 
  (-hash-code [this hash] (-hash-code (str this) hash))
  String
  (-hash-code [this hash] (hash this)))

(defn hash-code
  ([this] (hash-code this *hash*))
  ([this hash] (-hash-code this hash)))

(defn distance 
  ([p q] (distance p q *hash*))
  ([p q h] (bit-xor (hash-code p h)
                    (hash-code q h))))
