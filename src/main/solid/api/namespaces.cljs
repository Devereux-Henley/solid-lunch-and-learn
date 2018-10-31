(ns solid.api.namespaces
  (:require
   ["rdflib" :as rdflib]))

(def foaf (.Namespace rdflib "http://xmlns.com/foaf/0.1/"))
