(ns solid.api.mutations
  (:require
   [fulcro.client.primitives :as prim]
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.logging :as log]
   ))

;; Place your client mutations here

;; common
(defn upsert
  [table state value id]
  (assoc-in state [table id] value))

(defn delete
  [table state id]
  (update-in state [table] dissoc id))

;; persons
(def person-table  :person/by-id)
(def upsert-person (partial upsert person-table))
(def delete-person (partial delete person-table))

(defn upsert-me
  [state me id]
  (-> state
    (upsert-person me id)
    (assoc :authentication/me [person-table id])))

(defn delete-me
  [state id]
  (-> state
    (delete-person state id)
    (dissoc :authentication/me)))

(defn upsert-people
  [state people]
  (reduce (fn [acc [id data]] (upsert-person acc (assoc data :person/id id) id)) state people))

(defn delete-people
  [state]
  (assoc state :person/by-id {}))

;; sessions
(def session-table :session/by-id)
(def upsert-session (partial upsert session-table))
(def delete-session (partial delete session-table))

(defn upsert-solid-session
  [state session]
  (if-let [[_ id] (:authentication/solid-session state)]
    (upsert-session state session id)
    (let [id (random-uuid)]
      (-> state
        (upsert-session session id)
        (assoc :authentication/solid-session [session-table id])))))

(defn delete-solid-session
  [state]
  (if-let [[_ id] (:authentication/solid-session state)]
    (-> state (dissoc :authentication/solid-session) (delete-session id))
    state))

;; mutations

(defmutation upsert-me!
  "Upserts information for the current user."
  [{:keys [authentication/me]}]
  (action [{:keys [state] :as env}]
    (swap! state #(upsert-me % me (:person/id me)))))

(defmutation upsert-people!
  "Upserts multiple people."
  [{:keys [person/by-id]}]
  (action [{:keys [state] :as env}]
    (swap! state #(upsert-people % by-id))))

(defmutation set-solid-session!
  "Sets the solid session."
  [{:keys [solid/session]}]
  (action [{:keys [state] :as env}]
    (swap! state
      #(-> %
         (upsert-solid-session session)))))

(defmutation delete-solid-session!
  "Deletes the solid session."
  [_]
  (action [{:keys [state] :as env}]
    (swap! state
      #(-> %
         delete-solid-session
         (as-> % (delete-me % (second (:authentication/me %))))
         delete-people))))
