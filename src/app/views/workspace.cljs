(ns app.views.workspace
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljs-time.core :refer [today]]
            [app.views.workspaces.study :as study]
            [app.views.workspaces.edit :refer [edit-template]]
            [app.views.util.keyboard :as kbd]
            [app.views.data-table :refer [data-table]]
            [app.views.ui    :as ui]
            [app.views.icons :as icons]
            [app.views.card  :refer [render-card]]
            [app.styles     :as styles]
            [app.db         :refer [state ui-workspace where]]
            [app.models.card :as c]
            [app.events     :refer [dispatch]]))


;(extend-type js/FileList ISeqable)

;; -- DB Cursors -----------------------------------------------------------

(def all-fields (r/cursor state [:db :fields]))
(def all-cards (r/cursor state [:db :cards]))

(defn note [note-id]
  (let [note @(r/cursor state [:db :notes note-id])]
    [:div {:dangerouslySetInnerHTML {:__html
      (-> note :content js/marked)}}]))


(defn deck [deck-id]
  (let [deck        @(r/cursor state [:db :decks deck-id])
        deck-fields (->> @all-fields (where :deck-id deck-id) vals)
        cards       (->> @all-cards  (where :deck-id deck-id) vals)
        learned-cards (->> cards (filter #(-> % :reviews count (not= 0))) (sort-by :due))
        unlearned-cards (->> cards (filter #(-> % :reviews count (= 0))))]

    (js/console.log "found decks")

    [:<>
     [:div {:style {:margin-bottom "0.5em"}}
      
      [:div {:content-editable true
             :suppress-content-editable-warning true
             :on-blur #(dispatch [:db/edit-deck-name {
                                  :deck-id deck-id
                                  :name    (-> % .-target .-textContent)}])
             :style (conj styles/h1
                          {:outline 0
                           :-webkit-user-modify 'read-write-plaintext-only})}
       (deck :name)]]

     [ui/button [:<> [icons/pencil 14 14 5] "Edit Template"] #(dispatch [:ui/edit-deck-template deck-id])]
     [ui/button "Review" #(dispatch [:ui/review deck-id])]
     [ui/button "Learn" #(dispatch [:ui/learn deck-id])]
     [ui/button "Delete" #(dispatch [:db/delete-deck deck-id])]

     [data-table deck-fields (concat unlearned-cards learned-cards) deck-id]
     ]))


(defn home []
  [:div "Let's learn something!"])


(defn workspace []
  [:div {:style styles/workspace}
   [:div {:style styles/workspace-content}
    (case (first @ui-workspace)
      :home  [home]
      ;; TODO: it's not clear what nth is doing here.
      :note  [note  (nth @ui-workspace 1)]
      :deck  [deck  (nth @ui-workspace 1)]
      :edit-deck-template [edit-template (nth @ui-workspace 1)]
      :learn [study/learn (nth @ui-workspace 1)]
      :review [study/review (nth @ui-workspace 1)])]])
