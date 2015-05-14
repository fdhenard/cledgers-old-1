(ns cledgers.core
  (:require [org.httpkit.server :as http-kit-server]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [com.stuartsierra.component :as component]
            [hiccup.core :as hiccup]))

;; (:use [compojure.route :only [files not-found]]
;;       [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
;;       [compojure.core :only [defroutes GET POST DELETE ANY context]]
;;       org.httpkit.server)

(defn show-landing-page [req] ;; ordinary clojure function, accepts a request map, returns a response map
  ;; return landing page's html string. Using template library is a good idea:
  ;; mustache (https://github.com/shenfeng/mustache.clj, https://github.com/fhd/clostache...)
  ;; enlive (https://github.com/cgrand/enlive)
  ;; hiccup(https://github.com/weavejester/hiccup)
  (hiccup/html
   [:html
    [:head [:title "Cledgers Baby! Cledgers."]]
    [:body
     [:div {:id "root"}]
     [:script {:src "/js/app-cljs.js"}]
     [:script "cledgers.core.start()"]]]))

;; (defn update-userinfo [req]          ;; ordinary clojure function
;;   (let [user-id (-> req :params :id)    ; param from uri
;;         password (-> req :params :password)] ; form param
;;     ....
;;     ))

(defroutes all-routes
  (GET "/" [] show-landing-page)
  ;; (GET "/ws" [] chat-handler)     ;; websocket
  ;; (GET "/async" [] async-handler) ;; asynchronous(long polling)
  ;; (context "/user/:id" []
  ;;          (GET / [] get-user-by-id)
  ;;          (POST / [] update-userinfo))
  (route/resources "/")
  ;; (route/files "/static/") ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

;; (run-server (site #'all-routes) {:port 8080})

(defrecord Webapp []
  component/Lifecycle
  (start [component]
    (println ";; Starting Webapp")
    (assoc component :server (http-kit-server/run-server (site #'all-routes) {:port 8080})))
  (stop [component]
    (println ";; Stopping Webapp")
    (let [server (:server component)]
      (when-not (nil? server)
        (server :timeout 100)
        (assoc component :server nil)))))

(defn cledgers-system []
  (component/system-map
   :app (Webapp.)))

(defn -main [& args]
  (component/start (cledgers-system)))
