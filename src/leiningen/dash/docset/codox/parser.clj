(ns leiningen.dash.docset.codox.parser
  (:require [net.cgrand.enlive-html :as enlive]))

(def selectf (comp first enlive/select))

(defn id-attr [node]
  (first (enlive/attr-values node :id)))

(defn namespace-info [node]
  (if-some [name-node (selectf node [[:h2 :#top]])]
    {:name (enlive/text name-node)
     :type "Namespace"
     :path (str "#" (id-attr node))}))

(defn fn-info [node]
  (if (and (not (selectf node [(enlive/has [:h4])]))
           (selectf node [:div.usage :> :code]))
    {:name (enlive/text (selectf node [:h3]))
     :type "Function"
     :path (str "#" (id-attr node))}))

(defn var-info [node]
  (if (and (not (selectf node [:div.usage :> :code]))
           (not (selectf node [(enlive/has [:h4])]))
           (not (selectf node [[:h2 :#top]])))
    {:name (enlive/text (selectf node [:h3]))
     :type "Variable"
     :path (str "#" (id-attr node))}))

(defn protocol-info [node]
  (if-some [type-node (selectf node [[:h4.type]])]
    (if (= (enlive/text type-node) "protocol")
      {:name (enlive/text (selectf node [:h3]))
       :type "Protocol"
       :path (str "#" (id-attr node))})))

(defn macro-info [node]
  (if-some [type-node (selectf node [[:h4.type]])]
    (if (= (enlive/text type-node) "macro")
      {:name (enlive/text (selectf node [:h3]))
       :type "Macro"
       :path (str "#" (id-attr node))})))

(defn multimethod-info [node]
  (if-some [type-node (selectf node [[:h4.type]])]
    (if (= (enlive/text type-node) "multimethod")
      {:name (enlive/text (selectf node [:h3]))
       :type "Function"
       :path (str "#" (id-attr node))})))
