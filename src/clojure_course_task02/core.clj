(ns clojure-course-task02.core
  (:require [clojure-course-task02.search :as s])
  (:gen-class))

(defn find-files [file-name path]
  (s/search path file-name))

(defn usage []
  (println "Usage: $ run.sh file_name path"))

(defn -main [file-name path]
  (if (or (nil? file-name)
          (nil? path))
    (usage)
    (do
      (println "Searching for " file-name " in " path "...")
      (dorun (map println (find-files file-name path))))))
