(ns solid.api.mutations
  (:require
   [fulcro.client.primitives :as prim]
   [fulcro.client.mutations :as mutation :refer [defmutation]]
   [fulcro.client.logging :as log]))

;; Place your client mutations here

(defn upsert-session
  [state session id]
  (assoc-in state [:session/by-id id] session))

(defn upsert-solid-session!
  [state session]
  (swap! state
    #(if-let [[_ id] (:authentication/solid-session %)]
       (upsert-session % session id)
       (let [id (random-uuid)]
         (-> %
           (upsert-session session id)
           (assoc :authentication/solid-session [:session/by-id id]))))))

(defmutation set-solid-session!
  "Sets the solid session."
  [session]
  (action [{:keys [state] :as env}]
    (upsert-solid-session! state session)))
