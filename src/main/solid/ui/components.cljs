(ns solid.ui.components
  (:require
   [solid.api.mutations :as api]
   [fulcro.client.primitives :as prim :refer [defsc]]
   ["solid-auth-client" :as solid-client]
   [fulcro.client.dom :as dom]))

;; A good place to put reusable components
(defsc PlaceholderImage [this {:keys [w h label]}]
  (let [label (or label (str w "x" h))]
    (dom/svg {:width w :height h}
      (dom/rect {:width w :height h :style {:fill        "rgb(200,200,200)"
                                            :strokeWidth 2
                                            :stroke      "black"}})
      (dom/text {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label))))

(def ui-placeholder (prim/factory PlaceholderImage))

(defn authenticate [session]
  (if session
    session
    (.popupLogin solid-client #js {:popupUri "https://solid.community/common/popup.html"})))

(defn login [this]
  (-> (.currentSession solid-client)
    (.then #(authenticate %))
    (.then #(prim/transact! this `[(api/set-solid-session! ~(js->clj %))]))))

(defsc LoginButton [this {:keys [authentication/solid-session]}]
  {:query [[:authentication/solid-session '_]]}
  (when-not solid-session (dom/button {:onClick #(login this)} "Login")))

(def ui-login-button (prim/factory LoginButton))

(defn logout [this]
  (-> (.logout solid-client)
    (.then #(prim/transact! this `[(api/remove-solid-session! {})]))))

(defsc LogoutButton [this {:keys [authentication/solid-session]}]
  {:query [[:authentication/solid-session '_]]}
  (when solid-session (dom/button {:onClick #(logout this)} "Logout")))

(def ui-logout-button (prim/factory LogoutButton))
