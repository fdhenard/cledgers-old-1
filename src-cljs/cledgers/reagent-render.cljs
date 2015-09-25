(ns cledgers.reagent-render)

;; (def app-state (r/atom {:transactions
;;                         [
;;                          {:key 1 :payee "Erik Swanson" :amount 27.50}
;;                          ]}))
(def app-state (r/atom nil))

(defn update-xactions! [f & args]
  (log! (str "args = " (pr-str args)))
  (swap! app-state (fn [the-atom]
                     (-> the-atom
                         (update-in [:transactions] (fn [the-atom1] (apply f the-atom1 args)))
                         (assoc :updated-by :ui)
                         ))))

;; (defn update-xactions! [f & args]
;;   (apply swap! app-state update-in [:transactions] f args))

(defn add-xaction! [xaction]
  ;; (log! (str "adding xaction = " (pr-str xaction)))
  (let [next-key (+ 1 (apply max (map #(:key %) (:transactions @app-state))))]
    (update-xactions! conj (assoc xaction :key next-key))
    ;; (update-xactions! conj xaction)
    ))

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

(defn atom-input [the-atom & attribs-in]
  (let [final-attribs-in (or attribs-in {:type "text"})
        final-attribs (merge final-attribs-in
                             {:type "text"
                              :value @the-atom
                              :on-change #(reset! the-atom (-> % .-target .-value))})]
    (log! (with-out-str (pprint/pprint final-attribs)))
    [:input final-attribs]))

(defn new-xaction-repr []
  (let [payee-in (r/atom nil)
        amount-in (r/atom nil)]
    [:tr
     [:td [atom-input payee-in]]
     [:td [atom-input amount-in]]
     [:td [:button {:on-click #(add-xaction! {:payee @payee-in :amount @amount-in})} "Add"]]]))

(defn page [page-component]
  (r/render-component
   ;; (if (nil? (get-in app-state [:user]))
   ;;   (login-pg)
   ;;   [page-component])
   [page-component]
   (.getElementById js/document "root")))

(defn login-pg []
  (page
   (fn []
     (let [username (r/atom nil)]
       [:div {:class "container"}
        [:form
         [:h2 "Please sign in"]
         [:label {:class "sr-only" :for "input-email"} "Email address"]
         ;; [atom-input username {:id "input-email" :class "form-control" :placeholder "Email address"
         ;;                       :autofocus ""}]
         [:span "username input stub"]
         [:label {:class "sr-only" :for "input-password"}]
         [:input {:id "input-password" :class "form-control" :type "password" :placeholder "Password"}]
         [:button {:class "btn btn-lg btn-primary btn-block" :type "submit"} "Sign in"]
         ]]))))


(defn not-found-pg []
  (page [:h1 "404 - Page Not Found"]))

(defn xaction-list-repr-pg []
  (page
   [:div
    [:h1 "Transactions Baby"]
    [:table
     [:thead
      [:tr [:td "Payee"] [:td "Amount"]]]
     [:tbody
      (for [xaction (:transactions @app-state)]
        [xaction-repr xaction])
      [new-xaction-repr]]]]))

(add-watch app-state :app-state-change-logger
           (fn [key atom old-state new-state]
             (log! (str "app state change: " (pr-str new-state)))))

(def twriter (t/writer :json))

(add-watch app-state :app-state-transmitter
           (fn [key atom old-state new-state]
             (when (= (:updated-by new-state) :ui)
               (.send socket (t/write twriter new-state)))))
