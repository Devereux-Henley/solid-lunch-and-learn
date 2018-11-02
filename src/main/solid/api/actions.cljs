(ns solid.api.actions
  (:require
   [clojure.core.async :refer [go <! take! >! put! chan] :as async]
   [fulcro.client.primitives :as prim]
   [solid.api.mutations :as api]
   [solid.api.namespaces :as rdf-namespaces]
   [solid.api.rdf :as rdf]
   ["solid-auth-client" :as solid-client]
   ))

(defn load-from-session
  [component-or-reconciler session]
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
      (let [{:keys [person/name]} (<! channel)
            friends (<! (async/into [] channel))]
        (prim/transact! component-or-reconciler `[(api/set-solid-session! {:solid/session ~session})
                                                  (api/upsert-people!  {:person/by-id ~friends})
                                                  (api/upsert-me! {:authentication/me {:person/friends ~friends :person/id ~web-id :person/name ~name}})])
        ))))

(defn track-session
  [component-or-reconciler]
  (.trackSession
    solid-client
    #(if %
       (load-from-session component-or-reconciler (js->clj %))
       (prim/transact! component-or-reconciler `[(api/delete-solid-session! {})]))))
