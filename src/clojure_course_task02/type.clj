(ns clojure-course-task02.type)

(defn array-of
  [^java.lang.Class type]
  (-> type
      (make-array 0)
      class
      .getName))

(defmacro type-hint-array-of
  [t]
  (array-of (resolve t))
  )
