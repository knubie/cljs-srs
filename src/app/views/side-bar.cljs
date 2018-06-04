(ns app.views.side-bar
  (:require [reagent.core    :as r]
            [app.db          :refer [state all-decks all-notes]]
            [app.events      :refer [dispatch]]
            [app.views.icons :as icons]
            [app.styles      :as styles]))

(defn side-bar-section-item [{:keys [name icon on-click]}]
  [:div {:on-click on-click
                   :style {:display 'flex
                           :align-items 'center
                           :min-height 27
                           :font-size 14
                           :padding "2px 14px"
                           :width "100%"}}
             [:div {:style {:color "rgba(0,0,0,0.2)"
                            :margin-right 4}} [icon 18 18]]
             [:div {:style {:white-space 'nowrap
                            :overflow 'hidden 
                            :text-overflow 'ellipsis}}
              name]])

(defn side-bar-section [{:keys [title]} & children]
  [:div {:style {:margin-bottom 20}}
   [:div {:style {:display 'flex
                  :align-items 'center
                  :min-height 24
                  :font-size 14
                  :padding "0px 14px 0px 15px"
                  :width "100%"}}
    [:span {:style styles/side-bar-header} title]]
   children])

(defn side-bar []
  [:div {:style styles/side-bar}
   [:div
    [side-bar-section {:title "Notes"}
     (for [[id note] @all-notes] ^{:key (note :id)}
       [side-bar-section-item {:name (note :name)
                               :icon icons/doc-text
                               :on-click #(js/console.log "select-note")}])]

    [side-bar-section {:title "Decks"
                       :icon icons/book}
     (for [[id deck] @all-decks] ^{:key (deck :id)}
       [side-bar-section-item {:name (deck :name)
                               :icon icons/book
                               :on-click #(dispatch [:select-deck (deck :id)])}])]]])
