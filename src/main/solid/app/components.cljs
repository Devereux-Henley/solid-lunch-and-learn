(ns solid.app.components
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.dom :as dom]
   [fulcro-css.css-injection :as css-injection]
   [solid.lib.accounts.components :as accounts-ui]
   [solid.lib.authentication.components :as authentication-ui]
   [solid.lib.material.components :as material-ui]
   ))

(defsc ApplicationBar [this {:keys [login-button logout-button profile-badge]} computed {:keys [authentication-buttons]}]
  {:query         [:db/id
                   {:login-button (prim/get-query authentication-ui/LoginButton)}
                   {:logout-button (prim/get-query authentication-ui/LogoutButton)}
                   {:profile-badge (prim/get-query accounts-ui/ProfileBadge)}
                   ]
   :initial-state (fn [props] {:db/id         (prim/tempid)
                               :login-button  (prim/get-initial-state authentication-ui/LoginButton {})
                               :logout-button (prim/get-initial-state authentication-ui/LogoutButton {})
                               :profile-badge (prim/get-initial-state accounts-ui/ProfileBadge {})})
   :ident         [:application-bar/by-id :db/id]
   :css [[:.authentication-buttons {:flex-grow 1}]]}
  (material-ui/ui-app-bar #js {:position "static"}
    (material-ui/ui-toolbar
      (dom/div {:className authentication-buttons}
        (authentication-ui/ui-login-button login-button)
        (authentication-ui/ui-logout-button logout-button))
      (accounts-ui/ui-profile-badge profile-badge))))

(def ui-application-bar (prim/factory ApplicationBar))

(defsc Application [this {:keys [application-bar people authentication/me]}]
  {:query (fn [] [{:application-bar (prim/get-query ApplicationBar)}
                  {:people (prim/get-query accounts-ui/MyFriendList)}
                  [:authentication/me '_]])
   :ident (fn [] [:application :root])
   :initial-state (fn [props] {:application-bar (prim/get-initial-state ApplicationBar {})
                               :people (prim/get-initial-state accounts-ui/MyFriendList {})})}
  (dom/div nil
    (css-injection/style-element {:component this})
    (ui-application-bar application-bar)
    (when me (accounts-ui/ui-my-friend-list people))
    ))

(def ui-application (prim/factory Application))
