(ns leiningen.dash
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [leiningen.core.main :refer :all]
            [leiningen.doc :as codox]
            [leiningen.dash.docset.generator :refer :all])
  (:import [org.apache.commons.io.filefilter FileFilterUtils
                                             IOFileFilter
                                             NameFileFilter
                                             NotFileFilter
                                             TrueFileFilter
                                             WildcardFileFilter]
           [org.apache.commons.io FileUtils]))

(defn dash
  "Generate docset using Codox"
  [project & args]
  (do
    (info "Generating documentation with Codox ...")
    (codox/doc project)
    (let [doc-base-dir (io/file (get-in project [:codox :output-dir] "doc"))
          files-filter (FileFilterUtils/and
                         (into-array IOFileFilter [(WildcardFileFilter. "*.html")
                                                   (NotFileFilter. (NameFileFilter. "index.html"))]))
          files (iterator-seq (FileUtils/iterateFiles doc-base-dir files-filter TrueFileFilter/TRUE))]
      (info "Generating docset ...")
      (do
        (-> (create-docset-structure project)
            (copy-docs doc-base-dir)
            (transform-docset-html)
            (create-plist project)
            (create-db)
            (process-info (mapcat parse-file files)))))))
