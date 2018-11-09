(ns solid.app.root
  (:require
   [solid.app.components :as components]
   [fulcro.client.mutations :as m]
   [fulcro.client.data-fetch :as df]
   [fulcro.client.dom :as dom]
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.i18n :as i18n :refer [tr trf]]
   ))

;; The main UI of your application

(defsc Root [this {:keys [root/application]}]
  {:query (fn [] [{:root/application (prim/get-query components/Application)}])
   :initial-state (fn [props] {:ui/locale "en-US"
                               :root/application (prim/get-initial-state components/Application {})})}
  (dom/div nil
    (components/ui-application application)
    ))
