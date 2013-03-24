(ns clojure-course-task02.io
  (:require [clojure-course-task02.type :as type])
  (import java.io.File))

(defn list-files
  "Return list of files inside given directory."
  {:tag (type/type-hint-array-of File)}
  [^String dir-name]
  (.listFiles (File. dir-name))
  )

(defn absolute-file-name
  "Returns absolute file name for given file."
  ^String
  [^File file]
  (.getAbsolutePath file)
  )

(defn file-name
  "Returns short file name for given file"
  ^String
  [^File file]
  (.getName file)
  )
