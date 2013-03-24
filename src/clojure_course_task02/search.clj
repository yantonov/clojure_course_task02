(ns clojure-course-task02.search
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (:require [clojure-course-task02.type :as type])
  (import java.io.File)
  (import java.io.FileFilter)
  (import java.util.concurrent.RecursiveAction)
  (import java.util.concurrent.ForkJoinPool))

(defn only-files
  "Filter only files from array of File objects."
  {:tag (type/type-hint-array-of File)}
  [files]
  (filter (fn [^File f] (.isFile f)) files)
  )

(defn only-directories
  "Filter only directories from array of File objects."
  {:tag (type/type-hint-array-of File)}
  [files]
  (filter (fn [^File f] (.isDirectory f)) files)
  )

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
   ^FileFilter filter
   ^clojure.lang.IRef result]
  (proxy [RecursiveAction] []
    (compute []
      (let [ls (io/list-files dir)
            recursive-tasks (into-array RecursiveAction
                                        (map #(create-search-task (str dir File/separator (io/file-name %))
                                                                  filter
                                                                  result)
                                             (only-directories ls)))]
        (do
          (when (not (empty recursive-tasks))
            (RecursiveAction/invokeAll recursive-tasks)
            (update-result result
                           (into-array (util/filter-files filter
                                                          (only-files ls))))))))))

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
