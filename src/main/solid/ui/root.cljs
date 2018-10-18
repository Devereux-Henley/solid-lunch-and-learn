(ns solid.ui.root
  (:require
   [solid.ui.components :as components]
   [fulcro.client.mutations :as m]
   [fulcro.client.data-fetch :as df]
   [fulcro.client.dom :as dom]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.i18n :as i18n :refer [tr trf]]))

;; The main UI of your application

(defsc Root [this props]
  (dom/div nil
    (components/ui-login-button)))
