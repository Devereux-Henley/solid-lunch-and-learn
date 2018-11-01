(ns solid.ui.components
  (:require
   [solid.api.mutations :as api]
   [fulcro.client.primitives :as prim :refer [defsc]]
   ["solid-auth-client" :as solid-client]
   [fulcro.client.dom :as dom]
   [solid.ui.material :as material]
   ))

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

(defsc Application [this {:keys [application-bar]}]
  {:query (fn [] [{:application-bar (prim/get-query ApplicationBar)}])
   :ident (fn [] [:application :root])
   :initial-state (fn [props] {:application-bar (prim/get-initial-state ApplicationBar {})})}
  (dom/div nil
    (ui-application-bar application-bar)))

(def ui-application (prim/factory Application))
