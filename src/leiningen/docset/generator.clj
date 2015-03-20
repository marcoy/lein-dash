(ns leiningen.docset.generator
  (:require [net.cgrand.enlive-html :as enlive])
  (:import [java.io File]))

(defn selectf [nodes selector]
  (first (enlive/select nodes selector)))

(defn namespace-info [node]
  (if-some [name-node (selectf node [[:h2 :#top]])]
    {:name (enlive/text name-node)
     :type "namespace"
     :path ""}))

(defn fn-info [node]
  (if (and (not (selectf node [(enlive/has [:h4])]))
           (selectf node [:div.usage :> :code]))
    {:name (enlive/text (selectf node [:h3]))
     :type "function"
     :path ""}))

(defn var-info [node])

(defn protocol-info [node])

(defn macro-info [node])

(defn parse-file [^File html-file]
  (let [nodes (enlive/html-resource html-file)]
    (-> (enlive/select nodes [[:div :#content] :> [:h2 :#top]])
        first
        enlive/text)
    (map fn-info (enlive/select nodes [[:div :#content] :> :.anchor]))))
