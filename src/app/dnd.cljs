(ns app.dnd
  (:require [reagent.core :as r]
            [app.events      :refer [dispatch]]))

;; -- Rename stuff ---------------------------------------------------------

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

(defn drag-source [component opts]
  (((.-DragSource js/ReactDnD)
    (clj->js (opts :type))      ;; Type
    (clj->js (opts :drag-spec)) ;; Spec
    (fn [connect monitor]       ;; Collection Fn
      (clj->js ((opts :drag-collect) connect monitor)))

  ) component)
)

(defn as-drag-source [component opts]
  (-> component
      reagent->react
      (drag-source opts)
      react->reagent))


;; -- Drop Target ----------------------------------------------------------

(defn drop-target [component opts]
  (((.-DropTarget js/ReactDnD)
    (clj->js (opts :type))      ;; Type
    (clj->js (opts :drop-spec)) ;; Spec
    (fn [connect monitor]       ;; Collection Fn
      (clj->js ((opts :drop-collect) connect monitor)))

  ) component)
)

(defn as-drop-target [component opts]
  (-> component
      reagent->react
      (drop-target opts)
      react->reagent))


;; -- Both at the Same Time! -----------------------------------------------

(defn as-drag-source-and-drop-target [component opts]
  (-> component
      reagent->react
      (drag-source opts)
      (drop-target opts)
      react->reagent))
