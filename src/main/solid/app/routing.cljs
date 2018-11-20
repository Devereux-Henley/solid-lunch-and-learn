(ns solid.app.routing
  (:require
   [fulcro.client.routing :as routing]
   [fulcro.client.mutations :as mutations :refer [defmutation]]
   [pushy.core :as pushy]
   [bidi.verbose :refer [branch leaf param]]
   [bidi.bidi :as bidi]
   [fulcro.client.primitives :as prim]
   ))

;; Implementation from `https://github.com/fulcrologic/fulcro-template`

(def app-routing-tree
  (routing/routing-tree
    (routing/make-route :login [(routing/router-instruction :page-router [:login :page])])
    (routing/make-route :main [(routing/router-instruction :page-router [:main :page])])))

(def valid-handlers (-> (get app-routing-tree routing/routing-tree-key) keys set))

;; To keep track of the global HTML5 pushy routing object
(def history (atom nil))

;; To indicate when we should turn on URI mapping. This is so you can use with devcards (by turning it off)
(defonce use-html5-routing (atom true))

(def MAIN "index.html")
(def MAIN-URI (str "/" MAIN))
(def LOGIN "login.html")
(def LOGIN-URI (str "/" LOGIN))

(def app-routes
  "The bidi routing map for the application. The leaf keywords are the route names. Parameters
  in the route are available for use in the routing algorithm as :param/param-name."
  (branch
    "/"
    (leaf "" :main)
    (leaf MAIN :main)
    (leaf LOGIN :login)
    ))

(defn invalid-route?
  "Returns true if the given keyword is not a valid location in the routing tree."
  [kw]
  (not (contains? valid-handlers kw)))

(defn redirect*
  "Use inside of mutations to generate a URI redirect to a different page than you are on. Honors setting of use-html5-history.
  Use the plain function `nav-to!` for UI-level navigation."
  [state-map {:keys [handler route-params] :as bidi-match}]
  (if @use-html5-routing
    (let [path (apply bidi/path-for app-routes handler (flatten (seq route-params)))]
      (pushy/set-token! @history path)
      state-map)
    (routing/update-routing-links state-map bidi-match)))

(defn set-route!*
  "Implementation of choosing a particular bidi match. Used internally by the HTML5 history event implementation.
  Updates the UI only, unless the URI is invalid, in which case it redirects the UI and possibly generates new HTML5
  history events."
  [state-map {:keys [handler] :as bidi-match}]
  (cond
    (or (= :new-user handler) (= :login handler)) (routing/update-routing-links state-map bidi-match)
    (not (:logged-in? state-map))                 (-> state-map
                                                    (assoc :loaded-uri (when @history (pushy/get-token @history)))
                                                    (redirect* {:handler :login}))
    (invalid-route? handler)                      (redirect* state-map {:handler :main})
    :else                                         (routing/update-routing-links state-map bidi-match)))

(defmutation set-route!
  "Use `nav-to!` (as a plain function from the UI) instead of using this mutation directly.
     Set the route to the given bidi match. Checks to make sure the user is allowed to do so (are
     they logged in?). Sends them to the login screen if they are not logged in. This does NOT update the URI."
  [bidi-match]
  (action [{:keys [state]}] (swap! state set-route!* bidi-match)))

(defn nav-to!
  "Run a navigation mutation from the UI, but make sure that HTML5 routing is correctly honored so the components can be
  used as an app or in devcards. Use this in nav UI links instead of href or transact. "
  ([component page] (nav-to! component page {}))
  ([component page route-params]
   (if (and @history @use-html5-routing)
     (let [path (apply bidi/path-for app-routes page (flatten (seq route-params)))]
       (pushy/set-token! @history path))
     (prim/transact! component `[(set-route! ~{:handler page :route-params route-params}) :pages]))))

(defn start-routing [app-root]
  (when (and @use-html5-routing (not @history))
    (let [; NOTE: the :pages follow-on read, so the whole UI updates when page changes
          set-route! (fn [match]
                                        ; Delay. history events should happen after a tx is processed, but a set token could happen during.
                                        ; Time doesn't matter. This thread has to let go of the CPU before timeouts can process.
                       (js/setTimeout #(prim/transact! app-root `[(set-route! ~match) :pages])
                         10))]
      (reset! history (pushy/pushy set-route! (partial bidi/match-route app-routes)))
      (pushy/start! @history))))
