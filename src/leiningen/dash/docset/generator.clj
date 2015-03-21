(ns leiningen.dash.docset.generator
  (:require [clojure.java.io :as io]
            [leiningen.dash.docset.codox.parser :refer :all]
            [net.cgrand.enlive-html :as enlive]
            [yesql.core :refer [defqueries]])
  (:import [java.io File]
           [org.apache.commons.io FilenameUtils
                                  FileUtils]))

(defqueries "docset.sql")

(defn db-spec [path]
  {
   :classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     path
  })

(defn some-info [node fns]
  (let [[f & fs] fns]
    (if-some [info (f node)]
      info
      (if (some? fs)
        (recur node fs)))))

(defn parse-file [^File html-file]
  (let [nodes (enlive/html-resource html-file)]
    (map #(update-in
            (some-info % [namespace-info fn-info var-info protocol-info macro-info
                          multimethod-info])
            [:path]
            (fn [id] (str (FilenameUtils/getBaseName (.getAbsolutePath html-file))
                          id)))
         (enlive/select nodes [[:div :#content] :.anchor]))))

(defn create-docset-structure [project]
  (let [project-name (:name project)
        project-version (:version project)
        docset-dir (File. (format "%s-%s.docset/Contents/Resources/Documents"
                                  project-name project-version))]
    (FileUtils/forceMkdir docset-dir)
    docset-dir))

(defn copy-docs [docset-dir doc-base-dir]
  (FileUtils/copyDirectory doc-base-dir docset-dir)
  docset-dir)

(defn create-plist [docset-dir project]
  (let [project-name (:name project)
        plist (io/file (.getPath docset-dir) ".." ".." "Info.plist")
        plist-tpl (FileUtils/readFileToString (io/file (io/resource "Info.plist")) "UTF-8")]
    (FileUtils/writeStringToFile
      plist
      (format plist-tpl project-name project-name "clojure")
      "UTF-8")
    docset-dir))

(defn create-db [docset-dir]
  (let [db (io/file (.getPath docset-dir) ".." "docSet.dsidx")]
    (do
      (if (.exists db)
        (FileUtils/deleteQuietly db))
      (create-table! (db-spec (.getPath db)))
      (create-index! (db-spec (.getPath db)))
      db)))

(defn process-info [db-path infos]
  (let [spec (db-spec (.getPath db-path))]
    (doseq [i infos]
      (insert-info! spec (:name i) (:type i) (:path i)))
    db-path))
