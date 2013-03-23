(ns clojure-course-task02.util
  (:import java.io.File)
  (:import java.io.FileFilter)
  (:import java.util.regex.Pattern))

(defn create-filter
  "Returns file filter.
Returns filter accepts only files which names matches to given regular expression."
  ^FileFilter
  [regex]
  (let [pattern (Pattern/compile regex)]
    (proxy [FileFilter] []
      (accept [^File file]
        (-> pattern
            (.matcher (.getName file))
            (.matches))))))

(defn filter-files
  "Returns only filtered files"
  [filter-impl files]
  (filter #(.accept filter-impl %) files))
