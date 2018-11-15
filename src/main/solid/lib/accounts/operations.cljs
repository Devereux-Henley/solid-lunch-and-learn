(ns solid.lib.accounts.operations
  (:require
   [solid.lib.core.operations :as op-core]
   [solid.lib.accounts.schema :as schema-accounts]
   )
  )

(def upsert-person (partial op-core/upsert schema-accounts/people))
(def delete-person (partial op-core/delete schema-accounts/people))

(defn upsert-people
  [state people]
  (reduce (fn [acc {:keys [person/id] :as data}] (upsert-person acc (assoc data :person/id id) id)) state people))

(defn delete-people
  [state]
  (assoc state schema-accounts/people {}))
