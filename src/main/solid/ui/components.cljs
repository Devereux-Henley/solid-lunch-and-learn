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

(defsc FriendEntry [this {:keys [person/id person/name person/image]} computed {:keys [icon profile-image]}]
  {:query (fn [] (prim/get-query domain/Person))
   :ident [:person/by-id :person/id]
   :initial-state (fn [props] {:person/name "" :person/friends [] :person/id ""})
   :css [[:.icon {:height "40px" :width "40px"}]
         [:.profile-image {:margin-right "16px"}]]}
  (material/ui-list-item nil
    (material/ui-list-item-avatar nil
      (if image
        (material/ui-avatar #js {:src image :alt (str "Profile image of: " name) :className profile-image})
        (material/ui-list-item-icon nil
          (material/ui-icon-account-circle (clj->js {:classes {:root icon}})))))
    (material/ui-list-item-text #js {:primary name})))

(def ui-friend-entry (prim/factory FriendEntry {:keyfn :person/id}))

(defsc MyFriendList [this {:keys [authentication/me db/id]}]
  {:query (fn [] [:db/id {[:authentication/me '_] [{:person/friends (prim/get-query FriendEntry)}]}])
   :ident [:list/by-id :db/id]
   :initial-state (fn [props] {:db/id (prim/tempid)})}
  (dom/div nil
    (material/ui-typography #js {:variant "subheading"} "Friends: ")
    (material/ui-list nil (map #(ui-friend-entry %) (:person/friends me)))))

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

(defsc ProfileBadge [this {:keys [authentication/me]}]
  {:query [:db/id {[:authentication/me '_] [:person/image :person/name]}]
   :initial-state (fn [props] {:db/id (prim/tempid)})
   :ident [:person/by-id :db/id]}
  (dom/div nil
    (if me
      (material/ui-avatar #js {:src (:person/image me) :alt (str "Profile image of: " (:person/name me))})
      (material/ui-icon-account-circle nil))))

(def ui-profile-badge (prim/factory ProfileBadge))

(defsc ApplicationBar [this {:keys [login-button logout-button profile-badge]} computed {:keys [authentication-buttons]}]
  {:query         [:db/id
                   {:login-button (prim/get-query LoginButton)}
                   {:logout-button (prim/get-query LogoutButton)}
                   {:profile-badge (prim/get-query ProfileBadge)}
                   ]
   :initial-state (fn [props] {:db/id         (prim/tempid)
                               :login-button  (prim/get-initial-state LoginButton {})
                               :logout-button (prim/get-initial-state LogoutButton {})
                               :profile-badge (prim/get-initial-state ProfileBadge {})})
   :ident         [:application-bar/by-id :db/id]
   :css [[:.authentication-buttons {:flex-grow 1}]]}
  (material/ui-app-bar #js {:position "static"}
    (material/ui-toolbar
      (dom/div {:className authentication-buttons}
        (ui-login-button login-button)
        (ui-logout-button logout-button))
      (ui-profile-badge profile-badge))))

(def ui-application-bar (prim/factory ApplicationBar))

(defsc Application [this {:keys [application-bar people authentication/me]}]
  {:query (fn [] [{:application-bar (prim/get-query ApplicationBar)}
                  {:people (prim/get-query MyFriendList)}
                  [:authentication/me '_]])
   :ident (fn [] [:application :root])
   :initial-state (fn [props] {:application-bar (prim/get-initial-state ApplicationBar {})
                               :people (prim/get-initial-state MyFriendList {})})}
  (dom/div nil
    (css-injection/style-element {:component this})
    (ui-application-bar application-bar)
    (when me (ui-my-friend-list people))
    ))

(def ui-application (prim/factory Application))
