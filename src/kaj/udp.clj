(ns kaj.udp
  "simple udp stuff, server is used to create (in another thread) an udp server.
  the `send` function is used to send message to a specific address'' "
  (:refer-clojure :exclude [send])
  (:import (java.util Iterator)
           (java.net InetSocketAddress InetAddress)
           (java.nio ByteBuffer)
           (java.nio.charset Charset)
           (java.nio.channels ServerSocketChannel
                              Selector
                              SelectionKey
                              DatagramChannel
                              ClosedSelectorException)
           (java.util.concurrent Executors))
  (:require [taoensso.timbre :as log]))

(defn initialize
  "initialize objects for the server"
  [^Selector selector ^DatagramChannel channel ^InetSocketAddress address]
  (.configureBlocking channel false)
  (-> channel .socket (.bind address))
  (.register channel selector SelectionKey/OP_READ))

(defn select?
  "wrapper around `Selector.select()`"
  ([^Selector selector]
   (select? selector 0))
  ([^Selector selector timeout]
   (-> selector (.select timeout) pos?)))

(defmacro do-keys
  "similar to doseq, except this one is for `selectedKeys`.
  it has to be different because the nio lib uses the `Iterator.remove`
  function to remove an event from the queue."
  [[k ks] & b]
  `(let [^Iterator iter# (.iterator ~ks)]
    (while (.hasNext iter#)
      (let [^SelectedKey ~k (.next iter#)]
        (do ~@b)
        (.remove iter#)))))

(defn event-loop
  "run the event loop on the selector."
  [^Selector selector ^DatagramChannel channel handler timeout ^ByteBuffer buffer]
  (when (select? selector timeout)
    (do-keys [_ (.selectedKeys selector)]
             (.clear buffer)
             (.receive channel buffer)
             (.flip buffer)
             (handler buffer)))
  (recur selector channel handler timeout buffer))

(defn forever
  "wrapper around `event-loop`. mainly it wraps the event loop in a 
  try/catch that is triggered (the catch) when the selector is stopped
  (i.e. when the stop function is called. Being in a future, defererencing
  it is enough for waiting that the server is stopped."
  [^Selector selector ^DatagramChannel channel handler timeout ^ByteBuffer buffer]
  (future
    (try (event-loop selector channel handler timeout buffer)
         (catch ClosedSelectorException ex true))))

(defn stop
  "actually stop the server"
  [^Selector selector ^DatagramChannel channel]
  (-> channel .close)
  (-> selector .close)
  (-> selector .wakeup)
  true)

(defn server
  "creates a new server. the handler is a function that takes in input 
  the ByteBuffer. Remember that this buffer is unique within the server, 
  so don't read/write in other threads."
  [handler &
   {:keys [host port buffer-size address timeout]
    :or {address (InetSocketAddress. 34567)
         buffer-size 65535
         timeout 0}}]
  (let [buffer   (ByteBuffer/allocateDirect buffer-size)
        selector (Selector/open)
        channel  (DatagramChannel/open)
        ignore   (initialize selector channel address)
        promize  (forever selector channel handler timeout buffer)]
      (fn [] (stop selector channel) @promize)))

(defn send
  "wrapper around `Channel.send`"
  [^ByteBuffer message
   & {:keys [address]
      :or {address (InetSocketAddress. 34567)}}]
  (let [channel (DatagramChannel/open)]
    (.send channel message address)))
