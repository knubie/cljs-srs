(ns app.views.workspaces.deck
  (:require [reagent.core         :as r]
            [app.views.data-table :refer [data-table]]
            [app.styles           :as styles]
            [app.db               :as db]
            [app.views.util.helpers :refer [on-blur with-content]]
            [app.events           :refer [dispatch]]))

(defn deck-workspace [deck-id]
  (let [deck               (@db/all-decks deck-id)
        deck-fields        (r/track db/fields-for-deck deck-id)
        cards              (r/track db/cards-for-deck deck-id)
        learned-cards      (r/track db/learned-cards @cards)
        unlearned-cards    (r/track db/unlearned-cards @cards)
        all-cards          (concat @unlearned-cards @learned-cards)
        search-term        (r/atom "")
        update-search-term #(reset! search-term %)
        update-deck-name   #(dispatch [:db/edit-deck-name
                                       {:deck-id deck-id :name %}])]

    [:div {:style styles/workspace-content}
     [:div {:style {:flex "0 0 auto"}} (deck :id)]
     [:div {:style {:flex "0 0 auto" :margin-bottom "0.5em"}}
      [:div {:content-editable true
             :on-blur (on-blur (with-content update-deck-name))
             :style (merge styles/h1
                           styles/content-editable)}
       (deck :name)]]

     [:div {:content-editable true
            :on-change #(-> % .-target .-textContent update-search-term)
            :style (merge styles/content-editable
                         {:flex "0 0 auto"})}
      @search-term]

     [data-table @deck-fields all-cards deck-id]]))


(defn text? [field]
  (= (field :type) "text"))

(->> @all-cards
     (filter (-> % :fields
                 (some (fn [[field-id field-val]]
                         (and (-> @db/all-fields field-id text?)
                              (-> field-val (includes? @search-term))))))))

