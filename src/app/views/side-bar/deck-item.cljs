(ns app.views.side-bar.deck-item
  (:require [reagent.core    :as r]
            [app.dnd         :as dnd]
            [app.db          :refer [all-decks where]]
            [app.events      :refer [dispatch]]
            [app.views.icons :as icons]))

(def deck-dnd-opts
  {:type :deck-item
   :drag-collect (fn [connect monitor]
                   {:connectDragSource (.dragSource connect)})
 
   :drag-spec {:beginDrag (fn [props monitor component]
                            #js {:deckId (.-deckId props)})
               ;; If this becomes a clj map, we need to change how we access
               ;; it in the drop-spec/canDrop method.
 
               :endDrag (fn [props monitor component]
                          (js/console.log "end-drag")
                          (if (.didDrop monitor)
                            (dispatch [:db/nest-deck
                                       (keyword (.-deckId props))
                                       (keyword (.-deckId (.getDropResult monitor)))])

                            (js/console.log "Didn't drop")))}

   :drop-collect (fn [connect monitor] {:connectDropTarget (.dropTarget connect)
                                        :isOver (and (.isOver monitor #js {:shallow true})
                                                     (.canDrop monitor))})
   :drop-spec {:drop (fn [props monitor component]
                       (if (or (nil? component) (.didDrop monitor))
                         js/undefined
                         #js {:deckId (.-deckId props)}))

               :canDrop (fn [props monitor]
                          ;; TODO: Also need to test that parent can't be
                          ;; dragged onto children.
                          (not= (.-deckId props)
                                (.-deckId (.getItem monitor))))}

  }
)

(defn unwrapped-deck-item [props]
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
         [deck-item {:deck-id (:id child-deck)
                     :depth (+ depth 1)
                     :on-click #(dispatch [:ui/select-deck (child-deck :id)])}])]))))))

(def deck-item
  (dnd/as-drag-source-and-drop-target unwrapped-deck-item deck-dnd-opts))
