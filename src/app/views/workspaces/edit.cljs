(ns app.views.workspaces.edit
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [app.db         :refer [state]]
            [app.events     :refer [dispatch]]
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

(defn edit-note [note-id]
  (let [note @(r/cursor state [:db :notes note-id])]

    [:div {:style styles/workspace-content}
     [:div {:content-editable true
            :suppress-content-editable-warning true
            :on-blur #(dispatch [:db/edit-note
                                  {:note-id note-id
                                   :content (-> % .-target .-textContent)}])
            :style {:outline 0
                    :-webkit-user-modify 'read-write-plaintext-only}}

      (note :content)]]))
