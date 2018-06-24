(ns app.views.side-bar
  (:require [reagent.core    :as r]
            [app.db          :refer [state all-decks all-notes where]]
            [app.events      :refer [dispatch]]
            [app.views.side-bar.deck-item :refer [deck-item]]
            [app.views.side-bar.section :refer [section]]
            [app.views.icons :as icons]
            [app.views.util.keyboard :as kbd]
            [app.views.util.import :refer [import-deck]]
            [app.styles      :as styles]))

(def fs (js/require "fs"))

(defn side-bar-section-item [{:keys [name icon on-click]}]
  (r/with-let [background (r/atom "")]

    [:div {:on-click on-click
           :on-mouse-enter #(reset! background "rgba(58,56,52,0.08)")
           :on-mouse-leave #(reset! background "")
           :style {:display 'flex
                   :background @background
                   :align-items 'center
                   :min-height 27
                   :cursor 'pointer
                   :font-size 14
                   :padding "2px 14px"
                   :width "100%"}}
               [:div {:style {:color "rgba(0,0,0,0.2)"
                              :margin-right 4}} [icon 18 18]]
               [:div {:style {:white-space 'nowrap
                              :overflow 'hidden 
                              :text-overflow 'ellipsis}}
                name]]))

(defn side-bar []
  (let [top-level-decks (->> @all-decks (where :deck-id nil))]
    [:div {:style styles/side-bar}
     [:div
      [section {:title "Notes"}
       (for [[id note] @all-notes] ^{:key (note :id)}
         [side-bar-section-item {:name (note :name)
                                 :icon icons/doc-text
                                 :on-click #(dispatch [:ui/select-note (note :id)])}])
       [side-bar-section-item {:name "Add Note"
                               :key :new-note
                               :icon icons/plus
                               :on-click #(dispatch [:new-note])}]]

      [section {:title "Decks"
                         :icon icons/book}
       (for [[id deck] top-level-decks] ^{:key (deck :id)}
         [deck-item {:deck-id (:id deck)
                     :depth 1
                     :on-click #(dispatch [:select-deck (deck :id)])}])

       [side-bar-section-item {:name "Add Deck"
                               :key :new-deck
                               :icon icons/plus
                               :depth 1
                               :on-click #(dispatch [:new-deck])}]
       [import-deck]
       ]]]))
