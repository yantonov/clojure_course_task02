(ns clojure-course-task02.search
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (import java.io.File)
  (import java.io.FileFilter)
  (import java.util.concurrent.RecursiveAction)
  (import java.util.concurrent.ForkJoinPool))

(defn only-files
  "Filter only files from array of File objects."
  [files]
  (filter #(.isFile %) files)
  )

(defn only-directories
  "Filter only directories from array of File objects."
  [files]
  (filter #(.isDirectory %) files)
  )

(defn update-result [result files]
  (when (not (empty? files))
    (let [filenames (map #(io/file-name %) files)]
      (dosync
       (alter result #(concat % filenames))))))

(defn create-search-task
  "Create task to search files accepted by filter inside given dir."
  ^RecursiveAction
  [^String dir
   ^FileFilter filter
   result]
  (proxy [RecursiveAction] []
    (compute []
      (let [ls (io/list-files dir)
            inner-dirs (map #(io/absolute-file-name %)
                            (only-directories ls))]
        (do
          (update-result result
                         (util/filter-files filter
                                            (only-files ls)))
          (if (> (count inner-dirs) 0)
            (let [recursive-tasks (into-array (map #(create-search-task % filter result)
                                                   inner-dirs))]
              (RecursiveAction/invokeAll recursive-tasks))))))))

(defn search
  "Search files inside given directory using Fork join pool framework."
  [^String dir
   ^String filter-regex]
  (let [fjp (ForkJoinPool.)
        filter (util/create-filter filter-regex)
        result (ref [])
        task (create-search-task dir filter result)]
    (do (.invoke fjp task)
        @result)))
