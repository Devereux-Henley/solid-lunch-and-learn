(ns solid.lib.authentication.mutations
  (:require
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.primitives :as prim]
   [solid.lib.accounts.operations :as op-accounts]
   [solid.lib.authentication.operations :as op-authentication]
   [solid.lib.core.operations :as op-core]
   [solid.lib.authentication.schema :as schema-authentication]
   ))

;; mutations

;; me
(defmutation upsert-me!
  "Upserts information for the current user."
  [{:keys [authentication/me]}]
  (action [{:keys [state] :as env}]
    (swap! state #(op-authentication/upsert-me % me (:person/id me)))))

;; session
(defmutation set-solid-session!
  "Sets the solid session."
  [{:keys [solid/session]}]
  (action [{:keys [state] :as env}]
    (swap! state
      #(-> %
         (op-authentication/upsert-solid-session session)))))

(defmutation delete-solid-session!
  "Deletes the solid session."
  [_]
  (action [{:keys [state] :as env}]
    (swap! state
      #(-> %
         op-authentication/delete-solid-session
         (as-> % (op-authentication/delete-me % (second (schema-authentication/me %))))
         op-accounts/delete-people))))
