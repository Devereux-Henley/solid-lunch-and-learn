(ns solid.lib.authentication.actions
  (:require
   [clojure.core.async :as async]
   [fulcro.client.primitives :as prim]
   ["solid-auth-client" :as solid-client]
   [solid.lib.rdf.common :as common-rdf]
   [solid.lib.accounts.mutations :as api-accounts]
   [solid.lib.authentication.mutations :as api-authentication]
   ))

(defn authenticate [session]
  (if session
    session
    (.popupLogin solid-client #js {:popupUri "https://solid.community/common/popup.html"})))

(defn login [this]
  (-> (.currentSession solid-client)
    (.then #(authenticate %))
    (.then (constantly nil))))

(defn logout [this]
  (-> (.logout solid-client)
    (.then (constantly nil))))

(defn- get-user-from-store
  [web-id]
  {:person/id      web-id
   :person/name    (common-rdf/get-foaf-literal web-id "name")
   :person/image   (common-rdf/get-foaf-literal web-id "img")
   :person/friends []})

(defn load-from-session
  [component-or-reconciler session]
  (let [web-id  (get session "webId")
        channel (async/chan)]
    (-> (common-rdf/load web-id)
      (.then #(let [my-data           (get-user-from-store web-id)
                    friends           (map common-rdf/get-literal (common-rdf/find-each web-id (common-rdf/ns-foaf "knows")))
                    friends-coll      (if (coll? friends) friends [friends])
                    processed-friends (atom 0)]
                (async/put! channel my-data)
                (run!
                  (fn [friend]
                    (.then
                      (common-rdf/load friend)
                      (fn [_]
                        (async/put! channel [friend (get-user-from-store friend)])
                        (swap! processed-friends inc)
                        (when (= @processed-friends (count friends-coll))
                          (async/close! channel)))))
                  friends-coll))))
    (async/go
      (let [my-data       (async/<! channel)
            friends       (async/<! (async/into [] channel))
            friend-idents (mapv (fn [[friend-id _]] [:person/by-id friend-id]) friends)]
        (prim/transact! component-or-reconciler `[(api-authentication/set-solid-session! {:solid/session ~session})
                                                  (api-accounts/upsert-people!  {:person/by-id ~friends})
                                                  (api-authentication/upsert-me! {:authentication/me ~(assoc my-data :person/friends friend-idents)})])
        ))))

(defn track-session
  [component-or-reconciler]
  (.trackSession
    solid-client
    #(if %
       (load-from-session component-or-reconciler (js->clj %))
       (prim/transact! component-or-reconciler `[(api-authentication/delete-solid-session! {})]))))
