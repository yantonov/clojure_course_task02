(ns clojure-course-task02.agent-search
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (import java.io.File))

;;; dispatcher agent send messages to worker agents to process directories
;;; worker agent knows only about dispatcher agent, not other workers

(def ^:dynamic *workers-count* (.availableProcessors (Runtime/getRuntime)))
(def workers (atom []))
(def filtered-files (ref []))
(def progress (atom 0))
(def dispatcher-agent (agent nil))

(defn add-filtered-files
  [file-filter files]
  (when-let [ls (seq (map #(io/file-name %)
                          (util/filter-files file-filter files)))]
    (dosync (alter filtered-files concat ls))))

(declare read-dir)

(defn schedule-worker-jobs [jobs-count]
  (swap! progress + jobs-count))

(defn select-worker []
  (let [worker-index (rem (System/currentTimeMillis) *workers-count*)]
    (@workers worker-index)))

(defn dispatch-jobs-workers [dirs]
  (schedule-worker-jobs (count dirs))
  (doseq [d dirs]
    (send-off (select-worker) read-dir d)))

(defn job-done [worker]
  (swap! progress dec))

(defn notify-dispatcher-job-done [worker]
  (send dispatcher-agent
        (fn [a] (job-done worker))))

(defn notify-dispatcher-schedule-jobs [dirs]
  (send dispatcher-agent
        (fn [a] (dispatch-jobs-workers dirs))))

(defn read-dir
  [worker dir]
  (let [ls (io/list-files dir)
        {files :file dirs :dir} (util/split-files-and-directories ls)
        dir-names (map #(str dir
                             (File/separator)
                             (io/file-name %))
                       dirs)]
    (add-filtered-files worker files)
    (notify-dispatcher-schedule-jobs dir-names)
    (notify-dispatcher-job-done worker)
    worker))

(defn create-agent [file-filter index]
  (agent file-filter))

(defn init-workers [file-filter]
  (let [coll (vec (map (fn [i] (create-agent file-filter i))
                       (range *workers-count*)))]
    (reset! workers coll)))

(defn init [file-filter dir-to-search]
  (reset! progress 0)
  (dosync (ref-set filtered-files []))
  (init-workers file-filter)
  (notify-dispatcher-schedule-jobs (list dir-to-search))
  (await dispatcher-agent))

(defn wait-completion []
  (while (not (zero? @progress)) nil))

(defn find-files
  [^String regex
   ^String dir]
  (init (util/create-filter regex) dir)
  (wait-completion)
  @filtered-files)
