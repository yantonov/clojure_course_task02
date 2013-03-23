(ns clojure-course-task02.util-test
  (:require [clojure-course-task02.util :as target])
  (:require [clojure.test :as test])
  (import java.io.File))

(test/deftest file-filter-test
  (test/testing "File filter matching."
    (test/are [regex filename verdict]
              (= verdict (.accept (target/create-filter regex) (File. filename)))
              "readme\\.txt" "readme.txt" true
              ".*\\.txt" "readme.txt" true
              ".*el" "tmp.txt" false
              "prefix-.*" "prefix-blablabla.org" true)))

(test/deftest filter-files-test
  (test/testing "Filtering sequence of files."
    (test/are
     [regex files filtered]
     (= filtered
        (map #(.getName %)
             (target/filter-files
              (target/create-filter regex)
              files)
             ))
     ".*\\.clj"
     [(File. "file1.clj")
      (File. "file2.clj")
      (File. "tmp.txt")
      (File. "README")]
     ["file1.clj"
      "file2.clj"])))
