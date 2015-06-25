(ns cledgers.core
  (:require [org.httpkit.server :as hks]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [compojure.core :refer [defroutes GET POST DELETE ANY context]]
            [com.stuartsierra.component :as component]
            [hiccup.core :as hiccup]
            [cognitect.transit :as transit]
            [clojure.pprint :as pp]
            ;;[clojure.tools.logging :as log]
            [taoensso.timbre :as tlog]
            )
  (:import [java.io ByteArrayOutputStream ByteArrayInputStream]))

(tlog/set-config! [:appenders :spit :enabled?] true)
(tlog/set-config! [:shared-appender-config :spit-filename] "cledgers.log")

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
     [:script {:src "/js/out/app-cljs.js"}]
     [:script "cledgers.core.start()"]]]))

;; (defn update-userinfo [req]          ;; ordinary clojure function
;;   (let [user-id (-> req :params :id)    ; param from uri
;;         password (-> req :params :password)] ; form param
;;     ....
;;     ))

(defn transit-resp [dater]
  (let [transit-out (ByteArrayOutputStream. 4096)
        transit-writer (transit/writer transit-out :json)]
    (transit/write transit-writer dater)
    (let [resp (.toString transit-out)]
      ;; (tlog/debug (str "resp = " resp "; type = " (type resp)))
      (.reset transit-out)
      resp)))

(defn transit-read [json-in]
  (let [in (ByteArrayInputStream. (.getBytes json-in))
        reader (transit/reader in :json)]
    (transit/read reader)))

(defonce db (atom {:transactions [{:key 1 :payee "Erik Swanson" :amount 100.00}]}))

(defn transactions [req]
  (transit-resp (:transactions @db)))

(defn http-kit-unified-handler [req]
  (hks/with-channel req channel ; get the channel
    ;; communicate with client using method defined above
    ;; (tlog/debug "a request is in. here it is: " (with-out-str (pp/pprint req)))
    (if (hks/websocket? channel)
      (tlog/debug (str "Websocket channel: " (with-out-str (pp/pprint channel))))
      (tlog/debug (str "HTTP channel: " (with-out-str (pp/pprint channel)))))
    (let [str-to-send (transit-resp @db)]
      (tlog/debug (str "sending: " str-to-send))
      (hks/send! channel str-to-send))

    (hks/on-close channel (fn [status]
                        (tlog/info "channel closed")))
    (hks/on-receive channel (fn [data-str] ; data received from client
                              (tlog/debug (str "string in: " data-str))
                              (let [data (transit-read data-str)]
                                (tlog/debug (str "data in: " (with-out-str (pp/pprint data))))
                                (reset! db (dissoc data :updated-by)))
                              ;; (hks/send! channel data)
                              ))))

(defroutes all-routes
  (GET "/" [] show-landing-page)
  ;; (GET "/ws" [] chat-handler)     ;; websocket
  ;; (GET "/async" [] async-handler) ;; asynchronous(long polling)
  ;; (context "/user/:id" []
  ;;          (GET / [] get-user-by-id)
  ;;          (POST / [] update-userinfo))
  (GET "/ws" [] http-kit-unified-handler)
  ;; (GET "/transactions/" [] transactions)
  (route/resources "/")
  ;; (route/files "/static/") ;; static file url prefix /static, in `public` folder
  (route/not-found "<p>Page not found.</p>")) ;; all other, return 404

;; (run-server (site #'all-routes) {:port 8080})

(defrecord Webapp []
  component/Lifecycle
  (start [component]
    (tlog/info ";; Starting Webapp")
    (assoc component :server (hks/run-server (site #'all-routes) {:port 8080})))
  (stop [component]
    (tlog/info ";; Stopping Webapp")
    (let [server (:server component)]
      (when-not (nil? server)
        (server :timeout 100)
        (assoc component :server nil)))))

(defn cledgers-system []
  (component/system-map
   :app (Webapp.)))

(defn -main [& args]
  (component/start (cledgers-system)))
