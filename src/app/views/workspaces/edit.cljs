(ns app.views.workspaces.edit
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [app.db         :refer [state]]
            [app.events     :refer [dispatch]]
            ))


(defn edit-template [deck-id]
  (let [deck @(r/cursor state [:db :decks deck-id])]

    [:div {:content-editable true
           :suppress-content-editable-warning true
           :on-blur #(dispatch [:edit-deck-template
                                 {:deck-id deck-id
                                  :template (-> % .-target .-textContent)}])
           :style {:outline 0
                   :-webkit-user-modify 'read-write-plaintext-only}}

     (deck :template)]))

