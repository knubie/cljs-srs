(ns app.views.workspace
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljstache.core :refer [render]]
            [app.styles     :as styles]
            [app.db         :refer [state where today]]
            [app.events     :refer [dispatch]]))

;; -- DB Cursors -----------------------------------------------------------

(def all-fields (r/cursor state [:db :fields]))
(def all-cards (r/cursor state [:db :cards]))

(defn split-card [display]
  (str/split display #"---"))

(defn render-card [card display deck-fields]
  (-> display (render (into {}
                        (map (fn [[id field]]
                          [(-> field :name keyword) (-> card :fields id)]
                        ) deck-fields)))
      js/marked)
  )

(defn forgot-button [card-id]
  [:input {:type "button"
           :value "Forgot"
           :on-click #(dispatch [:review-card {:card-id card-id
                                               :remembered? false}])}])

(defn remembered-button [card-id]
  [:input {:type "button"
           :value "Remembered"
           :on-click #(dispatch [:review-card {:card-id card-id
                                               :remembered? true}])}])

(defn workspace-study [deck-id]
  (let [current-side (r/atom 1)]
    (fn [] ;; TODO: Use transducers here
      (let [due-card (->> @all-cards (where :deck-id deck-id)
                                     (where :due (today)) vals first)
              deck @(r/cursor state [:db :decks deck-id])
              ;; TODO: Use a tracker here?
              deck-fields (->> @all-fields (where :deck-id deck-id))]
             ;new-cards  (->> @db-cards (first 10))]
          [:div "Let's study!"
           (if due-card
             [:div
              [:div (due-card :id)]
              (for [side (->> deck :display split-card (take @current-side))]
                [:div {:dangerouslySetInnerHTML
                      {:__html (render-card due-card side deck-fields)}}])
              [forgot-button (due-card :id)]
              [remembered-button (due-card :id)]]
             ;]
              ;[:div {:dangerouslySetInnerHTML
                    ;{:__html (render-card due-card (deck :display) deck-fields)}}]]
             [:div "No Cards!"])]))))

(defn new-card-editor [new-card fields on-done]
  [:div
  (for [[id _] fields]
    ^{:key id}
    [:div
     [:label {:for id} (_ :name)]
     [:input {:type "text" :name id
              :on-change #(swap! new-card assoc-in [:fields id] (-> % .-target .-value))}]])

  [:input {:type "button" :value "Done"
           :on-click #(do (dispatch [:add-card @new-card])
                          (on-done))}]]
  
  )

(defn custom-row [props]
  (let [proops (assoc (js->clj props) :onClick #(js/console.log 'foo))]
    (r/create-element "tr" (clj->js proops))))
  ;(r/as-element
    ;[:tr props]))

(defn custom-cell [text record]
  (let [editing? (r/atom false)]
    (fn [text record]
      (if @editing?
        [:> antd.Input {:type      "text"
                 :autoFocus true
                 :on-blur   #(reset! editing? false)
                 :value     text}]
        [:div {:on-click #(reset! editing? true) } text]))))

(defn workspace-deck [deck-id]
  (let [adding-card?  (r/atom false)
        selected-card (r/atom nil)
        new-card      (r/atom {:deck-id deck-id})]

    (fn [id]
      (let [deck   @(r/cursor state [:db :decks deck-id])
            deck-fields (->> @all-fields (where :deck-id deck-id))
            cards  (->> @all-cards  (where :deck-id deck-id))]

        [:div {:style styles/workspace-content} 
         [:h1 (deck :name)]

         ;; TODO: selected-card disappears before modal fades out.
         [:> js/antd.Modal {:visible (boolean @selected-card)
                            :footer nil
                            :onCancel #(reset! selected-card nil)}
          [:div {:dangerouslySetInnerHTML
                {:__html (render-card (cards @selected-card) (deck :display) deck-fields)}}]]

         [:input {:type "button" :value "New Card"
                  :on-click #(reset! adding-card? true)}]

         [:input {:type "button" :value "New Field"
                  :on-click #(dispatch [:add-field {:name "Foobar" :type "text" :deck-id deck-id}])}]

         [:input {:type "button" :value "Study"
                  :on-click #(dispatch [:study (deck :id)])}]

         (if @adding-card?
           [new-card-editor new-card deck-fields #(reset! adding-card? false)])

         [:h2 "Cards"]

         [:> js/antd.Table {:bordered true :pagination false
                            :columns (for [[id _] deck-fields]
                                       {:title     (_ :name)
                                        :dataIndex id
                                        :key       id
                                        :render    (fn [text record] (r/as-element [custom-cell text record]))})
                            :dataSource (->> cards vals (map #(assoc (% :fields) :key (% :id))))
                            :components {:body {:row custom-row}}
                            :rowSelection {:selectedRowKeys [] ;; Custom selections
                                           :onChange (fn [s-keys s-rows]
                                                       (reset! selected-card (-> s-keys first keyword)))}}]]))))

(defn workspace-home []
  [:div {:style styles/workspace-content}
    "Let's learn something!"])

(defn workspace [[workspace-name & args]]
  [:div {:style styles/workspace}
    (case workspace-name
      :home      [workspace-home]
      :deck      [workspace-deck  (first args)]
      :study     [workspace-study (first args)])])
