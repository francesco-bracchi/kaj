(ns kaj.messages.cid)

(def mask 0xFFFFFFFF)

(defn make
  []
  (bit-xor (rand-int mask)
           (bit-and mask (System/currentTimeMillis))))
