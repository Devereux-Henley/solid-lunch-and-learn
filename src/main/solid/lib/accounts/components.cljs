(ns solid.lib.accounts.components
  (:require
   [fulcro.client.dom :as dom]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [solid.lib.accounts.domain :as domain-accounts]
   [solid.lib.material.components :as material-ui]
   ))

(defsc FriendEntry [this {:keys [person/id person/name person/image]} computed {:keys [icon profile-image]}]
  {:query (fn [] (prim/get-query domain-accounts/Person))
   :ident [:person/by-id :person/id]
   :initial-state (fn [props] {:person/name "" :person/friends [] :person/id ""})
   :css [[:.icon {:height "40px" :width "40px"}]
         [:.profile-image {:margin-right "16px"}]]}
  (material-ui/ui-list-item nil
    (material-ui/ui-list-item-avatar nil
      (if image
        (material-ui/ui-avatar #js {:src image :alt (str "Profile image of: " name) :className profile-image})
        (material-ui/ui-list-item-icon nil
          (material-ui/ui-icon-account-circle (clj->js {:classes {:root icon}})))))
    (material-ui/ui-list-item-text nil
      (material-ui/ui-typography {:component "a" :href id} name))))

(def ui-friend-entry (prim/factory FriendEntry {:keyfn :person/id}))

(defsc MyFriendList [this {:keys [authentication/me db/id]}]
  {:query (fn [] [:db/id {[:authentication/me '_] [{:person/friends (prim/get-query FriendEntry)}]}])
   :ident [:list/by-id :db/id]
   :initial-state (fn [props] {:db/id (prim/tempid)})}
  (dom/div nil
    (material-ui/ui-typography #js {:variant "subheading"} "Friends: ")
    (material-ui/ui-list nil (map #(ui-friend-entry %) (:person/friends me)))))

(def ui-my-friend-list (prim/factory MyFriendList))

(defsc ProfileBadge [this {:keys [authentication/me]}]
  {:query [:db/id {[:authentication/me '_] [:person/image :person/name]}]
   :initial-state (fn [props] {:db/id (prim/tempid)})
   :ident [:profile-badge/by-id :db/id]}
  (dom/div nil
    (if me
      (material-ui/ui-avatar #js {:src (:person/image me) :alt (str "Profile image of: " (:person/name me))})
      (material-ui/ui-icon-account-circle nil))))

(def ui-profile-badge (prim/factory ProfileBadge))
