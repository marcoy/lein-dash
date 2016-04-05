(ns leiningen.dash
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [leiningen.core.main :refer :all]
            [leiningen.codox :as codox]
            [leiningen.dash.docset.generator :refer :all]
            [clojure.string :as s]))

(defn ^:private html-files
  "Get all the non-index HTML files at a given base."
  [base-dir]
  (filter (fn [f] (let [name (.getName f)]
                    (and (s/ends-with? name "html")
                         (not (s/ends-with? name "index.html")))))
          (file-seq base-dir)))

(defn dash
  "Generate docset using Codox"
  [project & args]
  (info "Generating documentation with Codox ...")
  (codox/codox project)
  (let [doc-base-dir (io/file (get-in project [:codox :output-dir] "doc"))]
    (info "Generating docset ...")
    (-> (create-docset-structure project)
        (copy-docs doc-base-dir)
        (transform-docset-html)
        (create-plist project)
        (create-db)
        (process-info (mapcat parse-file (html-files doc-base-dir))))))
