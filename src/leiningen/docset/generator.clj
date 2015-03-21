(ns leiningen.docset.generator
  (:require [leiningen.docset.codox.parser :refer :all]
            [net.cgrand.enlive-html :as enlive])
  (:import [java.io File]
           [org.apache.commons.io FilenameUtils]))

(defn some-info [node fns]
  (let [[f & fs] fns]
    (if-some [info (f node)]
      info
      (if (some? fs)
        (recur node fs)))))

(defn parse-file [^File html-file]
  (let [nodes (enlive/html-resource html-file)]
    (map #(update-in
            (some-info % [namespace-info fn-info var-info protocol-info macro-info])
            [:path]
            (fn [id] (str (FilenameUtils/getBaseName (.getAbsolutePath html-file))
                          id)))
         (enlive/select nodes [[:div :#content] :.anchor]))))
