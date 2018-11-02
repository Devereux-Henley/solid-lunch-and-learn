(ns solid.ui.components
  (:require
   [solid.api.domain :as domain]
   [solid.api.mutations :as api]
   [fulcro.client.primitives :as prim :refer [defsc]]
   ["solid-auth-client" :as solid-client]
   [fulcro.client.dom :as dom]
   [solid.ui.material :as material]
   ))

(defsc FriendEntry [this {:keys [person/name]}]
  {:query (fn [] (prim/get-query domain/Person))
   :ident [:friend-entry/by-id :db/id]
   :initial-state (fn [props] (prim/get-initial-state domain/Person {}))}
  (dom/div nil name))

(def ui-friend-entry (prim/factory FriendEntry {:keyfn :person/id}))

(defsc MyFriendList [this {:keys [authentication/me db/id]}]
  {:query (fn [] [:db/id {[:authentication/me '_] [:person/friends]}])
   :ident [:list/by-id :db/id]
   :initial-state (fn [props] {:db/id (prim/tempid) :list/items []})}
  (dom/div nil (map #(ui-friend-entry (second %)) (:person/friends me))))

(def ui-my-friend-list (prim/factory MyFriendList))

(defn authenticate [session]
  (if session
    session
    (.popupLogin solid-client #js {:popupUri "https://solid.community/common/popup.html"})))

(defn login [this]
  (-> (.currentSession solid-client)
    (.then #(authenticate %))
    (.then (constantly nil))))

(defsc LoginButton [this {:keys [authentication/solid-session]}]
  {:query [:db/id [:authentication/solid-session '_]]
   :initial-state (fn [props] {:db/id                        (prim/tempid)
                               :authentication/solid-session nil})
   :ident [:login-button/by-id :db/id]}
  (when-not solid-session (material/ui-button {:onClick #(login this) :color "inherit"} "Login")))

(def ui-login-button (prim/factory LoginButton))

(defn logout [this]
  (-> (.logout solid-client)
    (.then (constantly nil))))

(defsc LogoutButton [this {:keys [authentication/solid-session]}]
  {:query         [:db/id [:authentication/solid-session '_]]
   :initial-state (fn [props] {:db/id                        (prim/tempid)
                               :authentication/solid-session nil})
   :ident         [:logout-button/by-id :db/id]}
  (when solid-session (material/ui-button {:onClick #(logout this) :color "inherit"} "Logout")))

(def ui-logout-button (prim/factory LogoutButton))

(defsc ApplicationBar [this {:keys [login-button logout-button]}]
  {:query         [:db/id
                   {:login-button (prim/get-query LoginButton)}
                   {:logout-button (prim/get-query LogoutButton)}]
   :initial-state (fn [props] {:db/id         (prim/tempid)
                               :login-button  (prim/get-initial-state LoginButton {})
                               :logout-button (prim/get-initial-state LogoutButton {})})
   :ident         [:application-bar/by-id :db/id]}
  (material/ui-app-bar #js {:position "static"}
    (material/ui-toolbar
      (ui-login-button login-button)
      (ui-logout-button logout-button))))

(def ui-application-bar (prim/factory ApplicationBar))

(defsc Application [this {:keys [application-bar people]}]
  {:query (fn [] [{:application-bar (prim/get-query ApplicationBar)}
                  {:people (prim/get-query MyFriendList)}])
   :ident (fn [] [:application :root])
   :initial-state (fn [props] {:application-bar (prim/get-initial-state ApplicationBar {})
                               :people (prim/get-initial-state MyFriendList {})})}
  (dom/div nil
    (ui-application-bar application-bar)
    (ui-my-friend-list people)
    ))

(def ui-application (prim/factory Application))
