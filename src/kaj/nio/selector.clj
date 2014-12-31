(ns kaj.nio.selector
  (:import (java.nio.channels Selector SelectionKey))
  (:require [kaj.nio.bitmap :as bmp]
            [taoensso.timbre :as log]))

(def ^:dynamic *timeout* nil)

(def ^:dynamic *selector* nil)

(defn make [] (Selector/open))
  
(defn- dispatch-event [key]
  (let [cb (.attachment key)
        ch (.channel key)
        ev (-> key .interestOps bmp/from-bitmap)]
    (cb ch ev)))

;; (defn dispatch-selection
;;   [sel]
;;   (dorun (map dispatch-event (.selectedKeys sel))))

;; the former doesn't work because the item has to be removed from the `ready` table
(defn- dispatch-selection
  [sel]
  (let [i (-> sel .selectedKeys .iterator)]
    (while (.hasNext i)
      (let [key (.next i)]
        (.remove i)
        (dispatch-event key)))))

(defn active? [sel]
  (not (empty? (.keys sel))))

(defn select
  ([sel] (select sel *timeout*))
  ([sel timeout]
   (if (number? timeout) 
     (.select sel timeout)
     (.select sel))))

(defn- step
  [sel]
  (select sel)
  (dispatch-selection sel))

(defn call-with-open-selector
  [sel fun]
  (try (fun sel)
       (finally
         (when (.isOpen sel) (.close sel)))))

(defmacro with-open-selector 
  [[sel expr] & body]
  `(call-with-open-selector ~expr (fn [~sel] ~@body)))

(defmacro with-selector [[expr] & body]
  `(with-open-selector [sel# ~expr]
     (binding [*selector* sel#] ~@body)))

(defn once 
  ([] (once *selector*))
  ([sel]
   (when (active? sel) 
     (step sel))))

(defn forever 
  ([] (forever *selector*))
  ([sel]
   (when (active? sel)
     (step sel)
     (recur sel))))
