(ns solid.api.domain
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   ))

(defsc Person [this props]
  {:query [:db/id :person/name :person/friends :person/id :person/image]
   :ident [:person/by-id :db/id]
   :initial-state (fn [props] {:person/name "" :person/friends [] :person/id "" :db/id (prim/tempid)})})

(defsc Session [this props]
  {:query [:db/id :credentialType :issuer :authorization :sessionKey :idClaims :webId :idp]
   :ident [:session/by-id :db/id]})
