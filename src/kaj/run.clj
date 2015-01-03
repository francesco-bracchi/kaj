(ns kaj.run
  (:require [kaj.nio.selector :as sel]
            [kaj.nio.channel  :as chan]
            [kaj.nio.datagram :as datagram]
            [kaj.nio.buffer   :as buf]
            [kaj.node         :as node]
            [kaj.encoding     :as enc]
            [taoensso.timbre  :as log]))

(defn message-handler [chan events]
  (log/info "Events:" events)
  (buf/clear!)
  (buf/read!)
  (log/info "Data:" (enc/decode))
  (buf/flip!)
  (throw (Error. "haha")))

(defn run
  ([] (run (node/make)))
  ([node]
   (node/with-node [node]
     (sel/with-selector [(sel/make)]
       (buf/with-buffer [(buf/make)]
         (chan/with-channel [(datagram/make (node/host) (node/port))]
           (datagram/set-option! :reuse-address true)
           (chan/register #'message-handler #{:read})
           (log/info "Listening @" (:host node) ":" (:port node))
           (log/info "Selector:" sel/*selector*)
           (log/info "Channel:" chan/*channel*)
           (log/info "Buffer:" buf/*buffer*)
           (sel/forever)))))))

(defn message-send! 
  [msg node]
  (buf/with-buffer [(buf/make)]
    (buf/flip!)
    (enc/encode msg)
    (buf/wr
    

