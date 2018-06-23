(ns app.dnd
  (:require [reagent.core :as r]
            [app.events      :refer [dispatch]]))

(def react->reagent r/adapt-react-class)
(def reagent->react r/reactify-component)

(def drag-drop-context
  (.-DragDropContext js/ReactDnD))

(def html-backend
  (.-default js/ReactDnDHTML5Backend))

(defn with-drag-drop-context [backend component]
  (-> component
      reagent->react
      ((drag-drop-context backend))
      react->reagent))

;; -- Drag Source ----------------------------------------------------------

(def drag-source
  (.-DragSource js/ReactDnD))

(def drag-spec #js {:beginDrag (fn [props monitor component]
                           #js {:deckId (.-deckId props)})

                    :endDrag (fn [props monitor component]
                               (js/console.log "endDarg")
                               (if (.didDrop monitor)
                                 (do (js/console.log (.getDropResult monitor))
                                 (dispatch [:nest-deck
                                            (keyword (.-deckId props))
                                            (keyword (.-deckId (.getDropResult monitor)))
                                            ]))
                                 ;(js/console.log (.getDropResult monitor))
                                 (js/console.log "Didn't drop")
                               )
                             )
                    })

(defn drag-collect [connect monitor] #js {:connectDragSource (.dragSource connect)})

(defn as-drag-source [component]
  (-> component
      reagent->react
      ((drag-source "deck-item" drag-spec drag-collect))
      react->reagent))

;; -- Drop Target ----------------------------------------------------------

(def drop-target
  (.-DropTarget js/ReactDnD))

(def drop-spec #js {:drop (fn [props monitor component]
                            (js/console.log (.-deckId props))
                            (if (.didDrop monitor)
                              js/undefined
                              #js {:deckId (.-deckId props)}))
                    :hover (fn [props monitor component]
                               (js/console.log (.canDrop monitor)))
                    :canDrop (fn [props monitor]
                               (not= (.-deckId props)
                                     (.-deckId (.getItem monitor))))})

(defn drop-collect [connect monitor] #js {:connectDropTarget (.dropTarget connect)
                            :isOver (and
                                      (.canDrop monitor)
                                      (.isOver monitor )
                                    )
                            })

(defn as-drop-target [component]
  (-> component
      reagent->react
      ((drop-target "deck-item" drop-spec drop-collect))
      react->reagent))

;; -- Both at the Same Time! -----------------------------------------------

(defn as-drag-source-and-drop-target [component]
  (-> component
      reagent->react
      ((drag-source "deck-item" drag-spec drag-collect))
      ((drop-target "deck-item" drop-spec drop-collect))
      react->reagent))
