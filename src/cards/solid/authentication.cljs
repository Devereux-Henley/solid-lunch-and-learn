(ns solid.authentication
  (:require
   [devcards.core :as rc :refer-macros [defcard]]
   [solid.lib.authentication.actions :as actions-authentication]
   [solid.lib.authentication.components :as ui-authentication]
   [solid.lib.authentication.mutations :as api-authentication]
   [fulcro.client.cards :refer [defcard-fulcro]]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.dom :as dom]
   ))

(defsc AuthenticationRoot [this {:keys [login-button logout-button]}]
  {:query [{:login-button (prim/get-query ui-authentication/LoginButton)}
           {:logout-button (prim/get-query ui-authentication/LogoutButton)}]}
  (dom/div nil
    (ui-authentication/ui-login-button login-button)
    (ui-authentication/ui-logout-button logout-button)))

(defcard-fulcro Authentication
  "Demonstrates the authentication buttons interacting with state."
  AuthenticationRoot
  {}
  {:inspect-data true
   :classname    "break-all"
   :fulcro {:started-callback (fn [{:keys [reconciler] :as app}]
                                (actions-authentication/track-session reconciler))}})
