(ns solid.lib.rdf.common
  (:require
   ["rdflib" :as rdflib]
   ))

;; Client
(def store (rdflib/IndexedFormula.))
(def fetcher (rdflib/Fetcher. store))

;; Solid Specification
(def ns-foaf (.Namespace rdflib "http://xmlns.com/foaf/0.1/"))
(def ns-ldp (.Namespace rdflib "http://www.w3.org/ns/ldp#"))
(def ns-owl (.Namespace rdflib "http://www.w3.org/2002/07/owl#"))
(def ns-auth-acl (.Namespace rdflib "http://www.w3.org/ns/auth/acl#"))
(def ns-auth-cert (.Namespace rdflib "http://www.w3.org/ns/auth/cert#"))
(def ns-dc-terms (.Namespace rdflib "http://purl.org/dc/terms/"))

;; Solid Recommendations
(def ns-vcard (.Namespace rdflib "https://www.w3.org/TR/vcard-rdf/"))

;; Operations
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

(defn get-foaf-literal
  [web-id rdf-key]
  (->> (ns-foaf rdf-key)
    (find-any web-id)
    get-literal
    ))
