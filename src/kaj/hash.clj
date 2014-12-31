(ns kaj.hash
  (:import (clojure.lang IFn)
           (java.security MessageDigest)
           (java.util UUID)))

(defn random-salt [] (str (UUID/randomUUID)))

(defn hash-string [type salt input]
  (let [digest (MessageDigest/getInstance (.toUpperCase (name type)))]
    (doto digest
      .reset
      (.update (.getBytes salt "UTF-8"))
      (.update (.getBytes input "UTF-8")))
    (BigInteger. (.digest digest))))

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
  ([salt] (make salt :sha-1))
  ([salt type] (Hash. salt type)))

(def ^:dynamic *hash* (make *ns*))

(defprotocol Hashable
  "something that can stay on the kademlia space (a key or a node)"
  (hash-code [this] [this hash]))

(extend-protocol Hashable
  Object 
  (hash-code [this] (hash-code this *hash*))
  String
  (hash-code [this hash] (hash this)))

