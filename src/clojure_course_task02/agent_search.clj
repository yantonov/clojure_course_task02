(ns clojure-course-task02.agent-search
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (import java.io.File))

(def ^:dynamic *workers-count* (.availableProcessors (Runtime/getRuntime)))
(def workers (atom []))
(def filtered-files (ref []))
(def progress (atom 0))
(def reduce-agent (agent 0))

(defn add-filtered-files
  [file-filter files]
  (when-let [ls (seq (map #(io/file-name %)
                          (util/filter-files file-filter files)))]
    (send reduce-agent
          (fn [a] (dosync
                   (alter filtered-files
                          concat
                          ls)) a))))

(declare read-dir)

(defn schedule-worker-jobs [jobs-count]
  (swap! progress + jobs-count))

(defn send-workers [dirs]
  (schedule-worker-jobs (count dirs))
  (doseq [d dirs]
    (let [worker-index (rem (System/currentTimeMillis) *workers-count*)
          worker (@workers worker-index)]

      (send-off worker read-dir d))))

(defn worker-job-done [worker]
  (swap! progress - 1))

(defn read-dir
  [worker dir]
  (let [ls (io/list-files dir)
        {files :file dirs :dir} (util/split-files-and-directories ls)
        dir-names (map #(str dir
                             (File/separator)
                             (io/file-name %))
                       dirs)]
    (add-filtered-files worker files)
    (send-workers dir-names)
    (worker-job-done worker)
    worker)
  )

(defn create-agent [file-filter index]
  (let [a (agent file-filter)]
    (add-watch a
               (keyword (str "watch-" index))
               (fn [key reference old-state new-state]
                 nil))
    a))

(defn init-workers [file-filter]
  (let [coll (vec (map (fn [i] (create-agent file-filter i))
                       (range *workers-count*)))]
    (swap! workers (fn [a] coll))))

(defn init [file-filter dir-to-search]
  (swap! progress (fn [_] 0))
  (dosync (ref-set filtered-files []))
  (init-workers file-filter)
  (send-workers (list dir-to-search))
  )

(defn wait-completion []
  (while (not (zero? @progress)) nil))

(defn find-files
  [^String regex
   ^String dir]
  (init (util/create-filter regex) dir)
  (wait-completion)
  @filtered-files)
