(ns solid.api.mutations
  (:require
   [clojure.core.async :refer [go <! take! >! put! chan] :as async]
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
  [state me id]
  (-> state
    (upsert-person me id)
    (assoc :authentication/me [person-table id])))

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
  (reduce (fn [acc [friend-id friend-data]]
            (-> acc (upsert-person friend-data friend-id) (upsert-friend [person-table friend-id] friend-id))) state friends))

(defn upload-friend-or-friends
  [state friend-or-friends]
  (condp #(%1 %2) friend-or-friends
    coll? (upload-friends state friend-or-friends)
    nil? state
    (upload-friends state [friend-or-friends])))

(defmutation set-solid-session!
  "Sets the solid session."
  [session]
  (action [{:keys [state] :as env}]
    (let [web-id  (get session "webId")
          channel (chan)]
      (-> (rdf/load web-id)
        (.then #(let [fullname          (rdf/get-literal (rdf/find-any web-id (rdf-namespaces/foaf "name")))
                      friends           (rdf/get-literal (rdf/find-any web-id (rdf-namespaces/foaf "knows")))
                      friends-coll      (if (coll? friends) friends [friends])
                      processed-friends (atom 0)]
                  (put! channel {:person/name fullname})
                  (run!
                    (fn [friend]
                      (.then
                        (rdf/load friend)
                        (fn [_]
                          (put! channel [friend {:person/name (rdf/get-literal (rdf/find-any friend (rdf-namespaces/foaf "name")))}])
                          (swap! processed-friends inc)
                          (when (= @processed-friends (count friends-coll))
                            (async/close! channel)))))
                    friends-coll))))
      (go
        (let [my-data (<! channel)
              friends (<! (async/into [] channel))]
          (swap! state
            #(-> %
               (upsert-solid-session session)
               (upsert-me my-data web-id)
               (upload-friend-or-friends friends)
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
