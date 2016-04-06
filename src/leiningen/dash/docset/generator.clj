(ns leiningen.dash.docset.generator
  (:require [clojure.java.io :as io]
            [leiningen.dash.docset.codox.parser :refer :all]
            [net.cgrand.enlive-html :as enlive]
            [yesql.core :refer [defqueries]])
  (:import [java.io File]
           [org.apache.commons.io.filefilter FileFilterUtils
                                             IOFileFilter
                                             NameFileFilter
                                             NotFileFilter
                                             TrueFileFilter
                                             WildcardFileFilter]
           [org.apache.commons.io FilenameUtils
                                  FileUtils
                                  IOUtils]))

(defqueries "docset.sql")

(defn db-spec [path]
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname path})

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
            (fn [id] (str (FilenameUtils/getName (.getAbsolutePath html-file))
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
        plist-tpl (IOUtils/toString (io/input-stream (io/resource "Info.plist")) "UTF-8")]
    (FileUtils/writeStringToFile
      plist
      (format plist-tpl project-name project-name "clojure")
      "UTF-8")
    docset-dir))

(defn create-db [docset-dir]
  (let [db (io/file (.getPath docset-dir) ".." "docSet.dsidx")
        opts {:connection (db-spec (.getPath db))}]
    (if (.exists db)
      (FileUtils/deleteQuietly db))
    (create-table! {} opts)
    (create-index! {} opts)
    db))

(defn process-info [db-path infos]
  (let [opts {:connection (db-spec (.getPath db-path))}]
    (doseq [i infos]
      (insert-info! (select-keys i [:name :type :path]) opts))
    db-path))

(defn transform-docset-html
  "Clean up the Codox documentation so it looks properly in Dash."
  [docset-dir]
  (let [f-filter (FileFilterUtils/and
                   (into-array IOFileFilter [(WildcardFileFilter. "*.html")
                                             (NotFileFilter. (NameFileFilter. "index.html"))]))
        files (iterator-seq
                (FileUtils/iterateFiles docset-dir f-filter TrueFileFilter/TRUE))]
    (doseq [file files]
      (as-> (enlive/html-resource file) nodes
        (enlive/at nodes
                   [:#namespaces] (enlive/do-> (enlive/content "")
                                               (enlive/remove-attr :id :class))
                   [:#header] (enlive/do-> (enlive/content "")
                                           (enlive/remove-attr :id :class))
                   [:#vars] (enlive/do-> (enlive/content "")
                                         (enlive/remove-attr :id :class)))
        (FileUtils/writeStringToFile file (apply str (enlive/emit* nodes)) "UTF-8")))
    docset-dir))
