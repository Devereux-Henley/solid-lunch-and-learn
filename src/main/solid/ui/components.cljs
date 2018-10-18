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
  (if (not session)
    (.popupLogin solid-client #js {:popupUri "https://solid.community/common/popup.html"})
    session))

(defn login [this]
  (-> (.currentSession solid-client)
    (.then #(authenticate %))
    (.then #(prim/transact! this `[(api/set-session! ~(js->clj %))]))))

(defsc LoginButton [this props]
  {:query [:authentication/solid-session]}
  (dom/button {:onClick #(login this)} "Login"))

(def ui-login-button (prim/factory LoginButton))
