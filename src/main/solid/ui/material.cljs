(ns solid.ui.material
  (:require
   [fulcro.client.dom :as dom]
   ["@material-ui/core" :as material-ui]
   ))

(defn ui-theme-provider [& args] (dom/macro-create-element material-ui/ThemeProvider args))
(defn ui-button [& args] (dom/macro-create-element material-ui/Button args))
(defn ui-app-bar [& args] (dom/macro-create-element material-ui/AppBar args))
(defn ui-toolbar [& args] (dom/macro-create-element material-ui/Toolbar args))
