(ns app.views.workspaces.deck
  (:require [reagent.core         :as r]
            [app.views.data-table :refer [data-table]]
            [app.styles           :as styles]
            [app.db               :as db]
            [app.events           :refer [dispatch]]))

(defn deck-workspace [deck-id]
  (let [deck            (@db/all-decks deck-id)
        deck-fields     (r/track db/fields-for-deck deck-id)
        cards           (r/track db/cards-for-deck deck-id)
        learned-cards   (r/track db/learned-cards @cards)
        unlearned-cards (r/track db/unlearned-cards @cards)
        all-cards       (concat @unlearned-cards @learned-cards)
        on-blur         #(dispatch [:db/edit-deck-name
                                    {:deck-id deck-id :name %}])]

    [:div {:style styles/workspace-content}
     [:div {:style {:margin-bottom "0.5em"}}
      [:div {:content-editable true
             :on-blur #(-> % .-target .-textContent on-blur)
             :style (conj styles/h1 styles/content-editable)}
       (deck :name)]]

     [data-table @deck-fields all-cards deck-id]]))

