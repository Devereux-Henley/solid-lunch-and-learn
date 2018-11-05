(ns solid.ui.components
  (:require
   [solid.api.domain :as domain]
   [solid.api.mutations :as api]
   [fulcro.client.primitives :as prim :refer [defsc]]
   ["solid-auth-client" :as solid-client]
   [fulcro.client.dom :as dom]
   [fulcro-css.css-injection :as css-injection]
   [solid.ui.material :as material]
   ))

(defsc FriendEntry [this {:keys [db/id person/name person/image]} computed {:keys [icon profile-image]}]
  {:query (fn [] (prim/get-query domain/Person))
   :ident [:person/by-id :db/id]
   :initial-state (fn [props] (prim/get-initial-state domain/Person {}))
   :css [[:.icon {:height "40px" :width "40px"}]
         [:.profile-image {:margin-right "16px"}]]}
  (material/ui-list-item nil
    (material/ui-list-item-avatar nil
      (if image
        (material/ui-avatar #js {:src image :alt (str "Profile image of: " name) :className profile-image})
        (material/ui-list-item-icon nil
          (material/ui-icon-account-circle (clj->js {:classes {:root icon}})))))
    (material/ui-list-item-text #js {:primary name})))

(def ui-friend-entry (prim/factory FriendEntry {:keyfn :db/id}))

(defsc MyFriendList [this {:keys [authentication/me db/id]}]
  {:query (fn [] [:db/id {[:authentication/me '_] [{:person/friends (prim/get-query FriendEntry)}]}])
   :ident [:list/by-id :db/id]
   :initial-state (fn [props] {:db/id (prim/tempid)})}
  (material/ui-list nil
    (map #(ui-friend-entry %) (:person/friends me))))

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
    (css-injection/style-element {:component this})
    (ui-application-bar application-bar)
    (ui-my-friend-list people)
    ))

(def ui-application (prim/factory Application))
