(ns clojure-course-task02.util
  (:require [clojure-course-task02.type :as type])
  (:import java.io.File))

(defn create-filter
  "Returns file filter.
Returns filter accepts only files which names matches to given regular expression."
  [^String regex]
  (let [pattern (re-pattern regex)]
    (fn [^File f]
      (.matches (re-matcher pattern (.getName f))))))

(defn filter-files
  "Returns only filtered files"
  {:tag (type/type-hint-array-of File)}
  [file-filter
   files]
  (filter file-filter files))

(defn split-files-and-directories
  "Returns map with two keys :file :dir.
Each key points to file handles (files/directories)."
  [files]
  (group-by (fn [^File f] (cond
                           (.isFile f) :file
                           (.isDirectory f) :dir))
            files))
