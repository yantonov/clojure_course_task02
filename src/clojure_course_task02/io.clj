(ns clojure-course-task02.io
  (import java.io.File))

(defn list-files
  "Return list of files inside given directory."
  [^String dir-name]
  (.listFiles (File. dir-name))
  )

(defn absolute-file-name
  "Returns absolute file name for given file."
  [^File file]
  ^String
  (.getAbsolutePath file)
  )

(defn file-name
  "Returns short file name for given file"
  [^File file]
  ^String
  (.getName file)
  )
