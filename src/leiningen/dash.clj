(ns leiningen.dash
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [leiningen.core.main :refer :all]
            [leiningen.codox :as codox]
            [leiningen.dash.docset.generator :as g]
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
  (let [doc-base-dir (io/file (get-in project [:codox :output-dir] "target/doc"))]
    (info "Generating docset ...")
    (-> (g/create-docset-structure project)
        (g/copy-docs doc-base-dir)
        (g/transform-docset-html)
        (g/create-plist project)
        (g/create-db)
        (g/process-info (mapcat g/parse-file (html-files doc-base-dir))))))
