(ns clojure-course-task02.util)

(defn create-filter
  "Returns file filter.
Returns filter accepts only files which names matches to given regular expression."
  ^java.io.FileFilter
  [regex]
  (let [pattern (java.util.regex.Pattern/compile regex)]
    (proxy [java.io.FileFilter] []
      (accept [^java.io.File file]
        (-> pattern
            (.matcher (.getName file))
            (.matches))))))

(defn filter-files
  "Returns only filtered files"
  [filter-impl files]
  (filter #(.accept filter-impl %) files))
