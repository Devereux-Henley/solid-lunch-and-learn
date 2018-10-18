(ns solid.api.mutations
  (:require
   [fulcro.client.mutations :refer [defmutation]]
   [fulcro.client.logging :as log]))

;; Place your client mutations here

(defmutation set-session!
  "Sets the solid session."
  [session]
  (action [{:keys [state] :as env}]
    (swap! state #(let [id (random-uuid)]
                    (-> %
                      (assoc-in [:session/by-id id] session)
                      (assoc :authentication/solid-session [:session/by-id id])
                      )))))
