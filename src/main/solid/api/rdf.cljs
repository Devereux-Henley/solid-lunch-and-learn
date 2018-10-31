(ns solid.api.rdf
  (:require
   ["rdflib" :as rdflib]))

(def store (.graph rdflib))
(def fetcher (rdflib/Fetcher. store))

(defn load [id]
  (.load fetcher id))

(defn rdf-symbol
  [sym]
  (.sym rdflib sym))

(defn find-any
  [sym relationship]
  (.any store (rdf-symbol sym) relationship))

(defn get-literal
  [literal]
  (and literal (.-value literal)))
