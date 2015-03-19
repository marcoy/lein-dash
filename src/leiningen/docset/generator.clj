(ns leiningen.docset.generator
  (:require [net.cgrand.enlive-html :as enlive])
  (:import [java.io File]))

(defn parse-file [^File html-file]
  (enlive/html-resource html-file))
