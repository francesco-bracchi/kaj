(ns kaj.message.spec
  (:refer-clojure :exclude [key])
  (:import [java.net InetAddress
            InetSocketAddress
            Inet6Address]
           [java.util UUID])
  (:require [octet.spec :as spec]
            [octet.core :as buf]))

(def uuid
  (reify
    spec/ISpecSize
    (size [_] 16)
    spec/ISpec
    (read [_ buf pos]
      (let [[r0 hi] (spec/read buf/int64 buf pos)
            [r1 lo] (spec/read buf/int64 buf (+ pos r0))]
        [(+ r0 r1) (UUID. hi lo)]))
    (write [_ buf pos uuid]
      (let [hi (.getMostSignificantBits uuid)
            lo (.getLeastSignificantBits uuid)
            w0 (spec/write buf/int64 buf pos hi)
            w1 (spec/write buf/int64 buf (+ pos w0) lo)]
        (+ w0 w1)))))

(def ^:const key-size 16)

(def key-bytes (buf/bytes key-size))

(def key
  (reify
    spec/ISpecSize
    (size [_] key-size)
    spec/ISpec
    (read [_ buf pos]
      (let [[r b] (spec/read key-bytes buf pos)]
        [r (BigInteger. b)]))
    (write [_ buf pos k]
      (let [^bytes dest (byte-array key-size)
            ^bytes src  (-> k biginteger .toByteArray)
            len  (count src)]
        (System/arraycopy src 0 dest (- key-size len) len)
        (spec/write key-bytes buf pos dest)))))

(def ^:const inet-address-size 16)

(def inet-address-bytes (buf/bytes inet-address-size))

(def inet-address
  (reify
    spec/ISpecSize
    (size [_] inet-address-size)
    spec/ISpec
    (read [_ buf pos]
      (let [[r bs] (spec/read inet-address-bytes buf pos)]
        [r (InetAddress/getByAddress bs)]))
    (write [_ buf pos addr]
      (let [src (.getAddress addr)
            len (count src)]
        (if (= len inet-address-size)
          (spec/write inet-address-bytes buf pos src)
          (let [dest (byte-array inet-address-size)]
            (System/arraycopy src 0 dest (- inet-address-size len) len)
            (spec/write inet-address-bytes buf pos dest)))))))

(def port buf/short)

(def ^:const inet-socket-address-size
  (+ (buf/size inet-address) (buf/size port)))

(def inet-socket-address
  (reify
    spec/ISpecSize
    (size [_] inet-socket-address-size)
    spec/ISpec
    (read [_ buf pos]
      (let [[r0 ^InetAddress host] (spec/read inet-address buf pos)
            [r1 ^int port] (spec/read port buf (+ pos r0))]
        [(+ r0 r1) (InetSocketAddress. host port)]))
    (write [_ buf pos addr]
      (let [w0 (spec/write inet-address buf pos (.getAddress addr))
            w1 (spec/write port buf (+ pos w0) (.getPort addr))]
        (+ w0 w1 )))))

(def action buf/int16)

(def version buf/int16)

(def signature buf/int64) ;; can be int32

(def ring buf/int16)

(def header
  (buf/spec 
   :signature signature            ;; 8
   :action action                  ;; 2 
   :version version                ;; 2
   :ring ring                      ;; 2
   :origin inet-socket-address     ;; 18
   :key key                        ;; 16
   ))

