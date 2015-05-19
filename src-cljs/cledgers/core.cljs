(ns cledgers.core
  (:require [reagent.core :as r]
            [cognitect.transit :as t]))

(enable-console-print!)

;; (def app-state (r/atom {:transactions
;;                         [
;;                          {:key 1 :payee "Erik Swanson" :amount 27.50}
;;                          ]}))
(def app-state (r/atom nil))

(defn update-xactions! [f & args]
  (apply swap! app-state update-in [:transactions] f args))

(defn add-xaction! [xaction]
  (update-xactions! conj xaction))

(defn remove-xaction! [xaction]
  (update-xactions! (fn [xactions]
                      (vec (remove #(= % xaction) xactions)))
                    xaction))


;; UI components

(defn xaction-repr [xaction]
  [:tr
   [:td (:payee xaction)]
   [:td (:amount xaction)]
   [:td
    [:button {:on-click #(remove-xaction! xaction)} "Delete"]]])

(defn xaction-list-repr []
  [:div
   [:h1 "Transactions Baby"]
   [:table
    [:thead
     [:tr [:td "Payee"] [:td "Amount"]]]
    [:tbody
     (for [xaction (:transactions @app-state)]
       [xaction-repr xaction])]]])

;; Reagent Render Root Component
(defn start []
  (r/render-component
   [xaction-list-repr]
   (.getElementById js/document "root")))



;; (def socket (atom nil))
(def treader (t/reader :json))
(defn log! [stringg]
  (.log js/console stringg))
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
