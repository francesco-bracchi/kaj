(ns kaj.message
  (:refer-clojure :exclude [send])
  (:import [java.util UUID])
  (:require [octet.core :as buf]
            [kaj.message.spec :as spec]))

(def action->code
  {:get        0x01
   :get-node   0x02
   :put        0x03
   :broadcast  0x04
   :ping       0x05})

(def code->action
  (into {} (map (fn [[k v]] [v k])) action->code))

(def default-version 0)

(defn preprocess
  [obj]
  (-> obj
      (update :version #(or %1 default-version))
      (update :action action->code)))

(defn postprocess
  [obj]
  (update obj :action code->action))

(defn encode
  ([obj] (encode obj (buf/allocate *max-buffer*)))
  ([obj buf] (encode obj buf preprocess))
  ([obj buf preprocess]
   (doto buf
     (buf/write! (preprocess obj) header))))

(defn decode
  ([bytebuffer] (decode bytebuffer postprocess))
  ([bytebuffer postprocess]
   (-> bytebuffer
       (buf/read header)
       postprocess)))

