(ns solid.lib.authentication.actions
  (:require
   [clojure.core.async :as async]
   [fulcro.client.primitives :as prim]
   [promesa.core :as promesa]
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
  (->> (.currentSession solid-client)
    (promesa/map #(authenticate %))
    (promesa/catch (constantly nil))))

(defn logout [this]
  (->> (.logout solid-client)
    (promesa/catch (constantly nil))))

(defn- get-user-from-store
  [web-id]
  {:person/id      web-id
   :person/name    (common-rdf/get-foaf-literal web-id "name")
   :person/image   (common-rdf/get-foaf-literal web-id "img")
   :person/friends []})

(defn- fetch-friends
  [web-id]
  (let [friends      (mapv common-rdf/get-literal (common-rdf/find-each web-id (common-rdf/ns-foaf "knows")))
        friends-coll (if (coll? friends) friends [friends])]
    (->> friends-coll
      (mapv (fn [friend] (common-rdf/load friend)))
      promesa/all
      (promesa/map (fn [_] friends-coll)))))

(defn load-session-data!
  [web-id channel]
  (do
    (->> (common-rdf/load web-id)
      (promesa/map (fn [_] (do
                             (async/put! channel (get-user-from-store web-id))
                             web-id)))
      (promesa/map fetch-friends)
      (promesa/map (fn [friends]
                     (async/put! channel friends)
                     (async/close! channel)))
      (promesa/catch (constantly nil)))
    channel))

(defn process-session-data!
  [web-id session channel]
  (async/go
    (let [my-data       (async/<! channel)
          friends       (async/<! channel)
          friend-idents (mapv (fn [[friend-id _]] [:person/by-id friend-id]) friends)]
      (prim/transact! component-or-reconciler `[(api-authentication/set-solid-session! {:solid/session ~session})
                                                (api-accounts/upsert-people!  {:person/by-id ~friends})
                                                (api-authentication/upsert-me! {:authentication/me ~(assoc my-data :person/friends friend-idents)})]))))

(defn load-from-session
  [component-or-reconciler session]
  (let [web-id  (get session "webId")]
    (->> (async/chan)
      (load-session-data! web-id)
      (process-session-data! web-id session)
      )))

(defn track-session
  [component-or-reconciler]
  (.trackSession
    solid-client
    #(if %
       (load-from-session component-or-reconciler (js->clj %))
       (prim/transact! component-or-reconciler `[(api-authentication/delete-solid-session! {})]))))
