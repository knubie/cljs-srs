(ns app.views.workspaces.edit
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [app.db         :refer [state]]
            [app.events     :refer [dispatch]]
            [cljsjs.draft-js]
            [cljsjs.antd]
            [app.styles     :as styles]))


(defn edit-template [deck-id]
  (let [deck @(r/cursor state [:db :decks deck-id])]

    [:div {:style styles/workspace-content}
     [:div {:content-editable true
            :suppress-content-editable-warning true
            :on-blur #(dispatch [:db/edit-deck-template
                                  {:deck-id deck-id
                                   :template (-> % .-target .-textContent)}])
            :style {:outline 0
                    :-webkit-user-modify 'read-write-plaintext-only}}

      (deck :template)]]))

;(defn edit-note [note-id]
  ;(let [note @(r/cursor state [:db :notes note-id])]

    ;[:div {:style styles/workspace-content}
     ;[:div {:content-editable true
            ;:suppress-content-editable-warning true
            ;:on-blur #(dispatch [:db/edit-note
                                  ;{:note-id note-id
                                   ;:content (-> % .-target .-textContent)}])
            ;:style {:outline 0
                    ;:-webkit-user-modify 'read-write-plaintext-only}}

      ;(note :content)]]))

(defn get-selected-text
  [editor-state]
  (let [selection-state (.getSelection editor-state)
        anchor-key      (.getAnchorKey selection-state)
        start           (.getStartOffset selection-state)
        end             (.getEndOffset selection-state)]

    (-> editor-state (.getCurrentContent)
                     (.getBlockForKey anchor-key)
                     (.getText)
                     (.slice start end))))

(defn edit-note [note-id]
  (r/with-let [note         @(r/cursor state [:db :notes note-id])
               pop-over-visibile? (r/atom false)
               editor-state (r/atom (.createEmpty js/Draft.EditorState))
               onChange     (fn [es]
                              (reset! pop-over-visibile?
                                      (not (empty? (get-selected-text es))))
                              (reset! editor-state es))]

    [:div {:style styles/workspace-content}
     [:> js/antd.Popover {:content "Foobar"
                          :visible @pop-over-visibile?
                          :onVisibleChange #(reset! pop-over-visibile? %)}
      [:div]]
     [:> js/Draft.Editor {:editorState @editor-state
                          :onChange onChange}]
     ]))
