(ns solid.lib.authentication.operations
  (:require
   [solid.lib.accounts.schema :as schema-accounts]
   [solid.lib.authentication.schema :as schema-authentication]
   [solid.lib.accounts.operations :as op-accounts]
   [solid.lib.core.operations :as op-core]
   ))

;; store operations

;; me
(defn upsert-me
  [state me id]
  (-> state
    (op-accounts/upsert-person me id)
    (assoc schema-authentication/me [schema-accounts/people id])))

(defn delete-me
  [state id]
  (-> state
    (op-accounts/delete-person state id)
    (dissoc schema-authentication/me)))

;; sessions
(def upsert-session (partial op-core/upsert schema-authentication/sessions))
(def delete-session (partial op-core/delete schema-authentication/sessions))

(defn upsert-solid-session
  [state session]
  (if-let [[_ id] (schema-authentication/solid-session state)]
    (upsert-session state session id)
    (let [id (random-uuid)]
      (-> state
        (upsert-session session id)
        (assoc schema-authentication/solid-session [schema-authentication/sessions id])))))

(defn delete-solid-session
  [state]
  (if-let [[_ id] (schema-authentication/solid-session state)]
    (-> state (dissoc schema-authentication/solid-session) (delete-session id))
    state))
