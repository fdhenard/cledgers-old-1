(ns cledgers.core
  (:require [reagent.core :as r]
            [cognitect.transit :as t]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljs.pprint :as pprint])
  (:import goog.History))

(enable-console-print!)

(defn log! [stringg]
  (.log js/console stringg))

;; (defn update-xactions! [f & args]
;;   (log! (str "args = " (pr-str args)))
;;   (swap! app-state (fn [the-atom]
;;                      (-> the-atom
;;                          (update-in [:transactions] (fn [the-atom1] (apply f the-atom1 args)))
;;                          (assoc :updated-by :ui)
;;                          ))))

;; ;; (defn update-xactions! [f & args]
;; ;;   (apply swap! app-state update-in [:transactions] f args))

;; (defn add-xaction! [xaction]
;;   ;; (log! (str "adding xaction = " (pr-str xaction)))
;;   (let [next-key (+ 1 (apply max (map #(:key %) (:transactions @app-state))))]
;;     (update-xactions! conj (assoc xaction :key next-key))
;;     ;; (update-xactions! conj xaction)
;;     ))

;; (defn remove-xaction! [xaction]
;;   (update-xactions! (fn [xactions]
;;                       (vec (remove #(= % xaction) xactions)))
;;                     xaction))


;; ;; UI components
;; (defn xaction-repr [xaction]
;;   [:tr
;;    [:td (:payee xaction)]
;;    [:td (:amount xaction)]
;;    [:td
;;     [:button {:on-click #(remove-xaction! xaction)} "Delete"]]])

;; (defn atom-input [the-atom & attribs-in]
;;   (let [final-attribs-in (or attribs-in {:type "text"})
;;         final-attribs (merge final-attribs-in
;;                              {:type "text"
;;                               :value @the-atom
;;                               :on-change #(reset! the-atom (-> % .-target .-value))})]
;;     (log! (with-out-str (pprint/pprint final-attribs)))
;;     [:input final-attribs]))

;; (defn new-xaction-repr []
;;   (let [payee-in (r/atom nil)
;;         amount-in (r/atom nil)]
;;     [:tr
;;      [:td [atom-input payee-in]]
;;      [:td [atom-input amount-in]]
;;      [:td [:button {:on-click #(add-xaction! {:payee @payee-in :amount @amount-in})} "Add"]]]))

;; (defn xaction-list-repr []
;;   [:div
;;    [:h1 "Transactions Baby"]
;;    [:table
;;     [:thead
;;      [:tr [:td "Payee"] [:td "Amount"]]]
;;     [:tbody
;;      (for [xaction (:transactions @app-state)]
;;        [xaction-repr xaction])
;;      [new-xaction-repr]]]])

;; Reagent Render Root Component
(defn start []
  (secretary/dispatch! "/"))

;; (defn login-pg []
;;   (let [username (r/atom nil)]
;;     [:div {:class "container"}
;;      [:form
;;       [:h2 "Please sign in"]
;;       [:label {:class "sr-only" :for "input-email"} "Email address"]
;;       ;; [atom-input username {:id "input-email" :class "form-control" :placeholder "Email address"
;;       ;;                       :autofocus ""}]
;;       [:span "username input stub"]
;;       [:label {:class "sr-only" :for "input-password"}]
;;       [:input {:id "input-password" :class "form-control" :type "password" :placeholder "Password"}]
;;       [:button {:class "btn btn-lg btn-primary btn-block" :type "submit"} "Sign in"]
;;       ]]))

;; (defn page [page-component]
;;   (r/render-component
;;    (if (nil? (get-in app-state [:user]))
;;      (login-pg)
;;      [page-component])
;;    (.getElementById js/document "root")))

;; (defn not-found []
;;   [:h1 "404 - Page Not Found"])

(secretary/set-config! :prefix "#")
(defroute "/" [] (cledger.reagent-render/xaction-list-repr))
(defroute "/login" [] (cledgers.reagent-render/login-pg))
(defroute "*" [] (cledger.reagent-render/not-found-pg))

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h (.setEnabled true)))

;; (def socket (atom nil))
(def treader (t/reader :json))
(defn handle-msg-event [event]
  (log! (str "event data as transit = "(.-data event)))
  (let [data-native (t/read treader (.-data event))]
    (log! (str "data native = " (pr-str data-native)))
    (reset! app-state data-native)))
;; (aset socket "onmessage" handle-event)

;; (defonce state-map )
;; (declare socket)
(defn state-repr [the-sock]
  (let [state-map {0 :connecting
                   1 :open
                   2 :closing
                   3 :closed}]
    (get state-map (aget the-sock "readyState"))))
(defn log-event! [event]
  ;;(log!)
  ;; (log! (str "event name = " (.-type event) "; state = " (get state-map (aget socket "readyState"))))
  (log! (str "event name = " (.-type event)
             "; state = " (state-repr (.-currentTarget event)))))

(def socket (js/WebSocket. "ws://localhost:8080/ws"))
(let [event-handlers [;; ["onmessage" handle-msg-event]
                      ["onmessage" handle-msg-event]
                      ["onclose" log-event!]
                      ["onerror" log-event!]
                      ["onopen" log-event!]]]
  (doseq [[event-name func-to-run] event-handlers]
    (log! (str "registering event" event-name))
    (aset socket event-name func-to-run)))

;; (add-watch app-state :app-state-change-logger
;;            (fn [key atom old-state new-state]
;;              (log! (str "app state change: " (pr-str new-state)))))

;; (def twriter (t/writer :json))

;; (add-watch app-state :app-state-transmitter
;;            (fn [key atom old-state new-state]
;;              (when (= (:updated-by new-state) :ui)
;;                (.send socket (t/write twriter new-state)))))
