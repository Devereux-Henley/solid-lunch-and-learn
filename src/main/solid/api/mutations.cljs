(ns solid.api.mutations
  (:require
   [fulcro.client.primitives :as prim]
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.logging :as log]))

;; Place your client mutations here

(defn upsert
  [table state value id]
  (assoc-in state [table id] value))

(defn delete
  [table state id]
  (update-in state [table] dissoc id))

(def session-table :session/by-id)
(def upsert-session (partial upsert session-table))
(def delete-session (partial delete session-table))

(defn upsert-solid-session!
  [state session]
  (swap! state
    #(if-let [[_ id] (:authentication/solid-session %)]
       (upsert-session % session id)
       (let [id (random-uuid)]
         (-> %
           (upsert-session session id)
           (assoc :authentication/solid-session [session-table id]))))))

(defmutation set-solid-session
  "Sets the solid session."
  [session]
  (action [{:keys [state] :as env}]
    (upsert-solid-session! state session)))

(defn delete-solid-session!
  [state]
  (swap! state
    #(if-let [[_ id] (:authentication/solid-session %)]
       (-> % (dissoc :authentication/solid-session) (delete-session id))
       %)))

(defmutation delete-solid-session
  "Deletes the solid session."
  [_]
  (action [{:keys [state] :as env}]
    (delete-solid-session! state)))
