(ns clojure-course-task02.util
  (:require [clojure-course-task02.type :as type])
  (:import java.io.File)
  (:import java.io.FileFilter)
  (:import java.util.regex.Pattern))

(defn create-filter
  "Returns file filter.
Returns filter accepts only files which names matches to given regular expression."
  ^FileFilter
  [^String regex]
  (let [pattern (Pattern/compile regex)]
    (proxy [FileFilter] []
      (accept [^File file]
        (-> pattern
            (.matcher (.getName file))
            (.matches))))))

(defn filter-files
  "Returns only filtered files"
  {:tag (type/type-hint-array-of File)}
  [^FileFilter filter-impl
   files]
  (filter #(.accept filter-impl %) files))
