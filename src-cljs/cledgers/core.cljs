(ns cledgers.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(def app-state (r/atom {:transactions
                        [
                         {:key 1 :payee "Erik Swanson" :amount 27.50}
                         ]}))

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

(def socket (js/WebSocket. "ws://localhost:8080/ws"))
