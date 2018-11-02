(ns solid.ui.components
  (:require
   [solid.api.domain :as domain]
   [solid.api.mutations :as api]
   [fulcro.client.primitives :as prim :refer [defsc]]
   ["solid-auth-client" :as solid-client]
   [fulcro.client.dom :as dom]
   [solid.ui.material :as material]
   ))

(defsc PersonEntry [this {:keys [person/name]}]
  {:query (fn [] (prim/get-query domain/Person))
   :ident (fn [] (prim/get-ident domain/Person))
   :initial-state (fn [props] (prim/get-initial-state domain/Person {}))}
  (dom/div nil name))

(def ui-person-entry (prim/factory PersonEntry {:keyfn :db/id}))

(defsc PersonList [this {:keys [list/items :db/id]}]
  {:query (fn [] [:db/id {:list/items (prim/get-query PersonEntry)}])
   :ident [:list/by-id :db/id]
   :initial-state (fn [props] {:db/id (prim/tempid) :list/items []})}
  (dom/div nil (map ui-person-entry items)))

(def ui-person-list (prim/factory PersonList))

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
                  {:people (prim/get-query PersonList)}])
   :ident (fn [] [:application :root])
   :initial-state (fn [props] {:application-bar (prim/get-initial-state ApplicationBar {})
                               :people (prim/get-initial-state PersonList {})})}
  (dom/div nil
    (ui-application-bar application-bar)
    (ui-person-list people)
    ))

(def ui-application (prim/factory Application))
