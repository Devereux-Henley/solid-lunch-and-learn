(ns solid.api.rdf
  (:require
   ["rdflib" :as rdflib]))

(def store (.graph rdflib))
(def fetcher (rdflib/Fetcher. store))

(defn load [id]
  (.load fetcher id))

(defn rdf-symbol
  [sym]
  (and sym (.sym rdflib sym)))

(defn find-any
  ([subject relationship]
   (find-any subject relationship nil))
  ([subject relationship object]
   (.any store
     (rdf-symbol subject)
     relationship
     (rdf-symbol object))))

(defn find-each
  ([subject relationship]
   (find-each subject relationship nil))
  ([subject relationship object]
   (.each store
     (rdf-symbol subject)
     relationship
     (rdf-symbol object))))

(defn find-statements-matching
  ([subject relationship]
   (find-statements-matching subject relationship nil))
  ([subject relationship object]
   (.statementsMatching store
     (rdf-symbol subject)
     relationship
     (rdf-symbol object))))

(defn get-literal
  [literal]
  (and literal (.-value literal)))
