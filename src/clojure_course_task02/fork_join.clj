(ns clojure-course-task02.fork-join
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (:require [clojure-course-task02.type :as type])
  (import java.io.File)
  (import java.util.concurrent.RecursiveAction)
  (import java.util.concurrent.ForkJoinPool))

(defn update-result
  [^clojure.lang.IRef result
   files]
  (when (not (empty? files))
    (let [filenames (map #(io/file-name %) files)]
      (dosync
       (alter result #(concat % filenames)))))
  )

(defn create-search-task
  "Create task to search files accepted by filter inside given dir."
  ^RecursiveAction
  [^String dir
   file-filter
   ^clojure.lang.IRef result]
  (proxy [RecursiveAction] []
    (compute []
      (let [ls (io/list-files dir)
            {files :file dirs :dir} (util/split-files-and-directories ls)
            recursive-tasks (map #(create-search-task (str dir
                                                           File/separator
                                                           (io/file-name %))
                                                      file-filter
                                                      result)
                                 dirs)]
        (do
          (when (not (empty? recursive-tasks))
            (RecursiveAction/invokeAll recursive-tasks))
          (update-result result
                         (into-array (util/filter-files file-filter files))))))))

(defn find-files
  "Search files inside given directory using Fork join pool framework."
  [^String filter-regex
   ^String dir]
  (let [fjp (ForkJoinPool.)
        file-filter (util/create-filter filter-regex)
        result (ref [])
        task (create-search-task dir file-filter result)]
    (do (.invoke fjp task)
        @result)))
