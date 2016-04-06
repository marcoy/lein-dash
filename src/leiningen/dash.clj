(ns leiningen.dash
  (:require [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [leiningen.core.main :refer :all]
            [leiningen.codox :as codox]
            [leiningen.dash.docset.generator :as g]
            [clojure.string :as s]))

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
        (g/process-info (mapcat g/parse-file (g/html-files doc-base-dir))))))
