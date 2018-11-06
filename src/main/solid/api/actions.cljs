(ns solid.api.actions
  (:require
   [clojure.core.async :refer [go <! take! >! put! chan] :as async]
   [fulcro.client.primitives :as prim]
   [solid.api.mutations :as api]
   [solid.api.namespaces :as rdf-namespaces]
   [solid.api.rdf :as rdf]
   ["solid-auth-client" :as solid-client]
   ))

(defn- get-foaf-literal
  [web-id rdf-key]
  (rdf/get-literal (rdf/find-any web-id (rdf-namespaces/foaf rdf-key))))

(defn- query-user-data
  [web-id]
  {:person/id      web-id
   :person/name    (get-foaf-literal web-id "name")
   :person/image   (get-foaf-literal web-id "img")
   :person/friends []})

(defn load-from-session
  [component-or-reconciler session]
  (let [web-id  (get session "webId")
        channel (chan)]
    (-> (rdf/load web-id)
      (.then #(let [my-data           (query-user-data web-id)
                    friends           (map rdf/get-literal (rdf/find-each web-id (rdf-namespaces/foaf "knows")))
                    friends-coll      (if (coll? friends) friends [friends])
                    processed-friends (atom 0)]
                (put! channel my-data)
                (run!
                  (fn [friend]
                    (.then
                      (rdf/load friend)
                      (fn [_]
                        (put! channel [friend (query-user-data friend)])
                        (swap! processed-friends inc)
                        (when (= @processed-friends (count friends-coll))
                          (async/close! channel)))))
                  friends-coll))))
    (go
      (let [my-data       (<! channel)
            friends       (<! (async/into [] channel))
            friend-idents (mapv (fn [[friend-id _]] [:person/by-id friend-id]) friends)]
        (prim/transact! component-or-reconciler `[(api/set-solid-session! {:solid/session ~session})
                                                  (api/upsert-people!  {:person/by-id ~friends})
                                                  (api/upsert-me! {:authentication/me ~(assoc my-data :person/friends friend-idents)})])
        ))))

(defn track-session
  [component-or-reconciler]
  (.trackSession
    solid-client
    #(if %
       (load-from-session component-or-reconciler (js->clj %))
       (prim/transact! component-or-reconciler `[(api/delete-solid-session! {})]))))
