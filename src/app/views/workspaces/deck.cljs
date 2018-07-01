(ns app.views.workspaces.deck
  (:require [reagent.core         :as r]
            [app.views.data-table :refer [data-table]]
            [app.views.ui         :as ui]
            [app.views.icons      :as icons]
            [app.styles           :as styles]
            [app.db               :refer [state where]]
            [app.events           :refer [dispatch]]))

(def all-fields (r/cursor state [:db :fields]))
(def all-cards (r/cursor state [:db :cards]))

(defn deck-workspace [deck-id]
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

     [data-table deck-fields (concat unlearned-cards learned-cards) deck-id]]))

