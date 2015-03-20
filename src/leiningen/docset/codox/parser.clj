(ns leiningen.docset.codox.parser
  (:require [net.cgrand.enlive-html :as enlive]))

(def selectf (comp first enlive/select))

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

(defn var-info [node]
  (if (and (not (selectf node [:div.usage :> :code]))
           (not (selectf node [(enlive/has [:h4])]))
           (not (selectf node [[:h2 :#top]])))
    {:name (enlive/text (selectf node [:h3]))
     :type "variable"
     :path ""}))

(defn protocol-info [node]
  (if-some [type-node (selectf node [[:h4.type]])]
    (if (= (enlive/text type-node) "protocol")
      {:name (enlive/text (selectf node [:h3]))
       :type "protocol"
       :path ""})))

(defn macro-info [node]
  (if-some [type-node (selectf node [[:h4.type]])]
    (if (= (enlive/text type-node) "macro")
      {:name (enlive/text (selectf node [:h3]))
       :type "macro"
       :path ""})))
