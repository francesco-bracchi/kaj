(def +project+ 'it.frbracch/kaj)

(def +version+ "0.0.1")

(def +description+ "A p2p")

(def +source-paths+ #{"src"})

(def +dependencies+ '[[org.clojure/clojure "1.8.0"]
                      [org.clojure/tools.nrepl "0.2.11"]
                      [org.clojure/core.async "0.2.374"]
                      [funcool/octet "0.2.0"]
                      [com.taoensso/timbre "4.2.0"]
                      [clansi "1.0.0"]
                      [prismatic/schema "1.0.4"]
                      [clojurewerkz/buffy "1.0.2"]])

(def +url+ (clojure.java.shell/sh "git" "config" "--get" "remote.origin.url"))

(def +license+ {"EPL" "http://www.eclipse.org/legal/epl-v10.html"})

(def +main+ 'it.frbracch.kaj.main)

(def +jarfile+ (format "kaj-%s.jar" +version+))

(set-env!
 :source-paths +source-paths+
 :dependencies +dependencies+)

(task-options!
 pom  {:project     +project+
       :version     +version+
       :description +description+
       ;; :url         +url+
       ;; :scm         {:url +url+}
       :license     +license+}
 
 aot  {:all true}
 
 jar  {:main +main+
       :file +jarfile+
       :manifest {"Application-Name" (str +project+)}})

(deftask build
  []
  (comp (aot) (pom) (uber) (jar)))


