(ns leiningen.dash.docset.generator
  (:require [clojure.java.io :as io]
            [leiningen.dash.docset.codox.parser :as p]
            [net.cgrand.enlive-html :as enlive]
            [yesql.core :refer [defqueries]]
            [clojure.string :as s])
  (:import [java.io File]
           [org.apache.commons.io FileUtils]))

(defqueries "docset.sql")

(defn db-opts [db]
  {:connection {:classname "org.sqlite.JDBC"
                :subprotocol "sqlite"
                :subname (.getPath db)}})

(defn write [path body]
  (with-open [f (io/writer path)]
    (.write f body)))

(defn html-files
  "Get all the non-index HTML files at a given base."
  [base-dir]
  (filter (fn [f]
            (let [name (.getName f)]
              (and (s/ends-with? name "html")
                   (not (s/ends-with? name "index.html")))))
          (file-seq base-dir)))

(defn parse-file [^File html-file]
  (let [nodes (enlive/html-resource html-file)]
    (map (fn [node]
           (update (p/some-info node)
                   :path
                   (fn [id] (str (.getName html-file) id))))
         (enlive/select nodes [[:div :#content] :.anchor]))))

(defn create-docset-structure [{:keys [name version]}]
  (let [docset-dir (-> "%s-%s.docset/Contents/Resources/Documents"
                       (format name version)
                       (io/file))]
    (.mkdirs docset-dir)
    docset-dir))

(defn copy-docs [docset-dir doc-base-dir]
  (FileUtils/copyDirectory doc-base-dir docset-dir)
  docset-dir)

(defn create-plist [docset-dir {:keys [name]}]
  (let [plist-path (io/file (.getPath docset-dir) ".." ".." "Info.plist")
        plist-tpl (slurp (io/resource "Info.plist"))]
    (write plist-path (format plist-tpl name name "clojure"))
    docset-dir))

(defn create-db [docset-dir]
  (let [db (io/file (.getPath docset-dir) ".." "docSet.dsidx")
        opts (db-opts db)]
    (.delete db)
    (create-table! {} opts)
    (create-index! {} opts)
    db))

(defn process-info [db-path infos]
  (doseq [{:keys [name type path]} infos]
    (let [row {:name name :type type :path path}]
      (insert-info! row (db-opts db-path))))
  db-path)

(def ^:private scrub
  (enlive/do-> (enlive/content "") (enlive/remove-attr :id :class)))

(defn transform-docset-html
  "Clean up the Codox documentation so it looks properly in Dash."
  [docset-dir]
  (doseq [file (html-files docset-dir)]
    (as-> (enlive/html-resource file) nodes
      (enlive/at nodes
                 [:#namespaces] scrub
                 [:#header] scrub
                 [:#vars] scrub)
      (write file (apply str (enlive/emit* nodes)))))
  docset-dir)
