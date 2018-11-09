(ns solid.lib.accounts.mutations
  (:require
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.primitives :as prim]
   [solid.lib.accounts.operations :as op-accounts]
   ))

;; people
(defmutation upsert-people!
  "Upserts multiple people."
  [{:keys [person/by-id]}]
  (action [{:keys [state] :as env}]
    (swap! state #(op-accounts/upsert-people % by-id))))
