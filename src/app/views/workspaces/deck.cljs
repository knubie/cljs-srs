(ns app.views.workspaces.deck
  (:require [reagent.core         :as r]
            [app.views.data-table :refer [data-table]]
            [app.views.ui         :as ui]
            [app.views.icons      :as icons]
            [app.styles           :as styles]
            [app.db               :as db]
            [app.events           :refer [dispatch]]
            [cljs-time.core       :as time]))

(defn deck-workspace [deck-id]
  (let [deck            (@db/all-decks deck-id)
        deck-fields     (r/track db/fields-for-deck deck-id)
        cards           (r/track db/cards-for-deck deck-id)
        learned-cards   (r/track db/learned-cards @cards)
        unlearned-cards (r/track db/unlearned-cards @cards)]


    [:<>

    [:div {:style styles/workspace-content}
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


     [data-table @deck-fields
                 (concat @unlearned-cards @learned-cards)
                 deck-id]]]))

