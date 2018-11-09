(ns solid.lib.authentication.components
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [solid.lib.authentication.actions :as actions]
   [solid.lib.material.components :as material-ui]
   ))

(defsc LoginButton [this {:keys [authentication/solid-session]}]
  {:query         [:db/id [:authentication/solid-session '_]]
   :initial-state (fn [props] {:db/id                        (prim/tempid)
                               :authentication/solid-session nil})
   :ident         [:login-button/by-id :db/id]}
  (when-not solid-session (material-ui/ui-button {:onClick #(actions/login this) :color "inherit"} "Login")))

(def ui-login-button (prim/factory LoginButton))

(defsc LogoutButton [this {:keys [authentication/solid-session]}]
  {:query         [:db/id [:authentication/solid-session '_]]
   :initial-state (fn [props] {:db/id                        (prim/tempid)
                               :authentication/solid-session nil})
   :ident         [:logout-button/by-id :db/id]}
  (when solid-session (material-ui/ui-button {:onClick #(actions/logout this) :color "inherit"} "Logout")))

(def ui-logout-button (prim/factory LogoutButton))
