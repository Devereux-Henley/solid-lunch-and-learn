(ns solid.intro
  (:require [devcards.core :as rc :refer-macros [defcard]]
            [solid.ui.components :as comp]))

(defcard LoginButton
  "# Login Button"
  (comp/ui-login-button {}))

(defcard LogoutButton
  "# Logout Button"
  (comp/ui-logout-button {:authentication/solid-session {}}))
