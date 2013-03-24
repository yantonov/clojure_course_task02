(ns clojure-course-task02.fork-join-test
  (:require [clojure-course-task02.fork-join :as target])
  (:use clojure.test))

(deftest find-files-test
  (testing "find-files count testing..."
    (is (<= 2 (count (target/find-files "^core.+" "./"))))))


(deftest find-files-test2
  (testing "find-files contents testing..."
    (let [res (apply hash-set (target/find-files "^core.+" "./"))]
      (is (contains? res "core.clj"))
      (is (contains? res "core_test.clj")))))
