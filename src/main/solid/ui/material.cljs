(ns solid.ui.material
  (:require
   [fulcro.client.dom :as dom]
   ["@material-ui/core" :as material-ui]
   ["@material-ui/icons/AccountCircle" :default icon-account-circle]
   ))

(defn ui-theme-provider [& args] (dom/macro-create-element material-ui/ThemeProvider args))
(defn ui-button [& args] (dom/macro-create-element material-ui/Button args))
(defn ui-app-bar [& args] (dom/macro-create-element material-ui/AppBar args))
(defn ui-toolbar [& args] (dom/macro-create-element material-ui/Toolbar args))

(defn ui-avatar [& args] (dom/macro-create-element material-ui/Avatar args))
(defn ui-icon [& args] (dom/macro-create-element material-ui/Icon args))
(defn ui-svg-icon [& args] (dom/macro-create-element material-ui/SvgIcon args))

(defn ui-list [& args] (dom/macro-create-element material-ui/List args))
(defn ui-list-subheader [& args] (dom/macro-create-element material-ui/ListSubheader args))
(defn ui-list-item [& args] (dom/macro-create-element material-ui/ListItem args))
(defn ui-list-item-avatar [& args] (dom/macro-create-element material-ui/ListItemAvatar args))
(defn ui-list-item-icon [& args] (dom/macro-create-element material-ui/ListItemIcon args))
(defn ui-list-item-text [& args] (dom/macro-create-element material-ui/ListItemText args))

;; Icons
(defn ui-icon-account-circle [& args] (dom/macro-create-element icon-account-circle args))
