(ns clojure-course-task02.agent-search
  (:require [clojure-course-task02.util :as util])
  (:require [clojure-course-task02.io :as io])
  (import java.io.File)
  (import java.io.FileFilter))

(def ^:dynamic *workers-count* (.availableProcessors (Runtime/getRuntime)))
(def workers (atom []))
(def filtered-files (ref []))
(def progress (atom 0))

(defn add-filtered-files
  [^FileFilter file-filter files]
  (let [new-files (filter (fn [^File f] (.accept file-filter f)) files)]
    (when (not (empty? new-files))
      (dosync
       (alter filtered-files
              concat
              (map #(io/file-name %) new-files))))))

(declare read-dir)

(defn schedule-worker-jobs [count]
  (swap! progress + count))

(defn send-workers [dirs]
  (schedule-worker-jobs (count dirs))
  (doseq [d dirs]
    (let [worker-index (rem (System/currentTimeMillis) *workers-count*)
          worker (@workers worker-index)]
      (send-off worker read-dir d)))
  )

(defn worker-job-done [worker]
  (swap! progress dec))

(defn read-dir
  [worker dir]
  (let [ls (io/list-files dir)
        f (util/only-files ls)
        d (map #(str dir (File/separator) (io/file-name %))
               (util/only-directories ls))]
    (add-filtered-files worker f)
    (send-workers d)
    worker))

(defn create-agent [file-filter index]
  (add-watch (agent file-filter)
             (keyword (str "watch-" index))
             (fn [key reference old-state new-state]
               (worker-job-done reference))))

(defn init-workers [file-filter]
  (let [coll (vec (map (fn [i] (create-agent file-filter i))
                       (range *workers-count*)))]
    (swap! workers (fn [a] coll))))

(defn init [file-filter dir-to-search]
  (swap! progress (fn [a] 0))
  (dosync (ref-set filtered-files []))
  (init-workers file-filter)
  (send-workers (list dir-to-search))
  )

(defn wait-completion []
  (while (not (zero? @progress)) 1)
  )

(defn find-files
  [^String regex
   ^String dir]
  (init (util/create-filter regex) dir)
  (wait-completion)
  @filtered-files)
