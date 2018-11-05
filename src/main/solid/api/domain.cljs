(ns solid.api.domain
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   ))

(defsc Person [this props]
  {:query [:person/name :person/friends :person/id :person/image]
   :ident [:person/by-id :person/id]
   :initial-state {:person/name "" :person/friends [] :person/id ""}})

(defsc Session [this props]
  {:query [:db/id :credentialType :issuer :authorization :sessionKey :idClaims :webId :idp]
   :ident [:session/by-id :db/id]})
