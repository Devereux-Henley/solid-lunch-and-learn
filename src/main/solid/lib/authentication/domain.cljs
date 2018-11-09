(ns solid.lib.authentication.domain
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   ))

(defsc Session [this props]
  {:query [:db/id :credentialType :issuer :authorization :sessionKey :idClaims :webId :idp]
   :ident [:session/by-id :db/id]})
