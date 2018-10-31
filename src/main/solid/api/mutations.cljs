(ns solid.api.mutations
  (:require
   [clojure.core.async :refer [go <! take! >! put! chan]]
   [fulcro.client.primitives :as prim]
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.logging :as log]
   [solid.api.rdf :as rdf]
   [solid.api.namespaces :as rdf-namespaces]))

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

(defn upsert-me
  [state person]
  (if-let [[_ id] (:authentication/me state)]
    (upsert-person state person id)
    (let [id (random-uuid)]
      (-> state
        (upsert-person person id)
        (assoc :authentication/me [person-table id])))))

;; friends
(def friend-table  :friend/by-id)
(def upsert-friend (partial upsert friend-table))
(def delete-friend (partial delete friend-table))

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

(defn upload-friends
  [state friends]
  (reduce #(-> %1 (upsert-person {} %2) (upsert-friend [person-table %2] %2)) state friends))

(defn upload-friend-or-friends
  [state friend-or-friends]
  (condp #(%1 %2) friend-or-friends
    vector? (upload-friends state friend-or-friends)
    nil? state
    (upload-friends state [friend-or-friends])))

(defmutation set-solid-session!
  "Sets the solid session."
  [session]
  (action [{:keys [state] :as env}]
    (let [web-id (get session "webId")
          c      (chan)]
      (-> (rdf/load web-id)
        (.then #(put! c {:solid/fullname (rdf/find-any web-id (rdf-namespaces/foaf "name"))
                         :solid/friends  (rdf/find-any web-id (rdf-namespaces/foaf "knows"))})))
      (go
        (let [{:keys [solid/fullname solid/friends]} (<! c)]
          (swap! state
            #(-> %
               (upsert-solid-session session)
               (upsert-me {:person/name (and fullname (.-value fullname))})
               (upload-friend-or-friends (and friends (.-value friends)))
               )))))))

(defn delete-solid-session
  [state]
  (if-let [[_ id] (:authentication/solid-session state)]
    (-> state (dissoc :authentication/solid-session) (delete-session id))
    state))

(defmutation delete-solid-session!
  "Deletes the solid session."
  [_]
  (action [{:keys [state] :as env}]
    (swap! state
      #(delete-solid-session %))))
