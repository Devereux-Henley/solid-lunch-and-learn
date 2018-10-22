(ns solid.intro
  (:require
   [devcards.core :as rc :refer-macros [defcard]]
   [solid.ui.components :as components]
   [fulcro.client.cards :refer [defcard-fulcro]]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.dom :as dom]))

(defcard LoginButton
  "Creates a SOLID session for a user by presenting them with a login popup.

   Visible when there is no current SOLID session."
  (components/ui-login-button {}))

(defcard LogoutButton
  "Removes a users SOLID session.

   Visible when there is a current SOLID session."
  (components/ui-logout-button {:authentication/solid-session {}}))

(defsc AuthenticationRoot [this {:keys [login-button logout-button]}]
  {:query [{:login-button (prim/get-query components/LoginButton)}
           {:logout-button (prim/get-query components/LogoutButton)}]}
  (dom/div nil
    (components/ui-login-button login-button)
    (components/ui-logout-button logout-button)))

(defcard-fulcro AuthenticationButtons
  "Demonstrates the authentication buttons interacting with each others state."
  AuthenticationRoot
  {}
  {:inspect-data true})
