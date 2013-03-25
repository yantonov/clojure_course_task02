(ns clojure-course-task02.core
  (:require [clojure-course-task02.fork-join :as fj])
  (:require [clojure-course-task02.agent-search :as as])
  (:gen-class))

(defn find-files [file-name path]
  ;; fork join terrible solution
  (fj/find-files file-name path)

  ;; another agent based terrible solution
  ;; (as/find-files file-name path)
  )

(defn usage []
  (println "Usage: $ run.sh file_name path"))

(defn -main [file-name path]
  (if (or (nil? file-name)
          (nil? path))
    (usage)
    (do
      (println "Searching for " file-name " in " path "...")
      (dorun (map println (find-files file-name path))))))
