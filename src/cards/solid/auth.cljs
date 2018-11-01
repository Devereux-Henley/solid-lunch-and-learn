(ns solid.auth
  (:require
   [devcards.core :as rc :refer-macros [defcard]]
   ["solid-auth-client" :as solid-client]
   [solid.api.mutations :as api]
   [solid.ui.components :as components]
   [fulcro.client.cards :refer [defcard-fulcro]]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.dom :as dom]))

(defsc AuthenticationRoot [this {:keys [login-button logout-button]}]
  {:query [{:login-button (prim/get-query components/LoginButton)}
           {:logout-button (prim/get-query components/LogoutButton)}]}
  (dom/div nil
    (components/ui-login-button login-button)
    (components/ui-logout-button logout-button)))

(defcard-fulcro Authentication
  "Demonstrates the authentication buttons interacting with state."
  AuthenticationRoot
  {}
  {:inspect-data true
   :classname    "break-all"
   :fulcro {:started-callback (fn [{:keys [reconciler] :as app}]
                                (.trackSession
                                  solid-client
                                  #(if %
                                     (prim/transact! reconciler `[(api/set-solid-session! ~(js->clj %))])
                                     (prim/transact! reconciler `[(api/delete-solid-session! {})]))))}})
