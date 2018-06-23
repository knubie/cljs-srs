(ns app.views.side-bar
  (:require [reagent.core    :as r]
            [app.dnd         :as dnd]
            [app.db          :refer [state all-decks all-notes where]]
            [app.events      :refer [dispatch]]
            [app.views.icons :as icons]
            [app.views.util.keyboard :as kbd]
            [app.styles      :as styles]))

(defn side-bar-deck-item [props]
  (r/with-let [background (r/atom "")]

    (let [deck ((keyword (:deckId props)) @all-decks)
          child-decks (->> @all-decks (where :deck-id (:id deck)))
          depth (:depth props)
          on-click (:onClick props)
          connect-drag-source (:connectDragSource props)
          connect-drop-target (:connectDropTarget props)
          is-over (:isOver props)]

      (connect-drag-source (connect-drop-target (r/as-element
      [:div {:style {:background (if is-over "rgba(78, 188, 221, 0.3)" nil)}}
        [:div {:on-click on-click
               :on-mouse-enter #(reset! background "rgba(58,56,52,0.08)")
               :on-mouse-leave #(reset! background "")
               :style {:display 'flex
                       :background @background
                       :align-items 'center
                       :min-height 27
                       :cursor 'pointer
                       :font-size 14
                       :padding (str "2px 14px 2px " (* 14 depth) "px")
                       :width "100%"}}
                   [:div {:style {:color "rgba(0,0,0,0.2)"
                                  :margin-right 4}}
                    [icons/book 18 18]]

                   [:div {:style {:white-space 'nowrap
                                  :overflow 'hidden 
                                  :text-overflow 'ellipsis}}
                    (:name deck)]]
        (for [[id child-deck] child-decks] ^{:key (child-deck :id)}
         [(dnd/as-drag-source-and-drop-target side-bar-deck-item) {:deck-id (:id child-deck)
                              :depth (+ depth 1)
                              :on-edit #(dispatch [:edit-deck-name {
                                :deck-id id
                                :name    (-> % .-target .-textContent)}])
                              :on-click #(dispatch [:select-deck (child-deck :id)])}])]))))))


(defn side-bar-section-item [{:keys [name icon on-click on-edit]}]
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
               [:div {;:content-editable true
                      ;:suppress-content-editable-warning true
                      :on-blur on-edit
                      :on-key-down #(case (.-which %)
                                      kbd/enter  (-> % .-target .blur)
                                      kbd/escape (-> % .-target .blur)
                                      nil)
                      :style {;:-webkit-user-modify 'read-write-plaintext-only
                              :outline 0
                              :white-space 'nowrap
                              :overflow 'hidden 
                              :text-overflow 'ellipsis}}
                name]]))

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

(def new-deck
  {:name     "New Deck"
   :template "#{{Front}}\n\n---\n\n#{{Back}}"})

(def new-note
  {:name    "New Note"
   :content "Edit me!"})

(defn side-bar []
  (let [top-level-decks (->> @all-decks (where :deck-id nil))]
    [:div {:style styles/side-bar}
     [:div
      [side-bar-section {:title "Notes"}
       (for [[id note] @all-notes] ^{:key (note :id)}
         [side-bar-section-item {:name (note :name)
                                 :icon icons/doc-text
                                 :on-click #(dispatch [:ui/select-note (note :id)])}])
       [side-bar-section-item {:name "Add Note"
                               :icon icons/plus
                               :on-click #(dispatch [:add-note new-note])}]]

      [side-bar-section {:title "Decks"
                         :icon icons/book}
       (for [[id deck] top-level-decks] ^{:key (deck :id)}
         [(dnd/as-drag-source-and-drop-target side-bar-deck-item) {:deck-id (:id deck)
                                            :class "foo"
                              :depth 1
                              :on-edit #(dispatch [:edit-deck-name {
                                :deck-id id
                                :name    (-> % .-target .-textContent)}])
                              :on-click #(dispatch [:select-deck (deck :id)])}])
       [side-bar-section-item {:name "Add Deck"
                               :icon icons/plus
                               :depth 1
                               :on-click #(dispatch [:add-deck new-deck])}]]]]))
