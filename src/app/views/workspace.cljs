(ns app.views.workspace
  (:require [clojure.string             :as str] 
            [reagent.core               :as r]
            [app.views.workspaces.study :as study]
            [app.views.workspaces.edit  :refer [edit-template edit-note]]
            [app.views.workspaces.deck  :refer [deck-workspace]]
            [app.views.topbar           :as topbar]
            [app.styles                 :as styles]
            [app.events                 :refer [dispatch]]
            [app.db                     :as db]))


;(extend-type js/FileList ISeqable)

;; -- DB Cursors -----------------------------------------------------------

(defn note-workspace [note-id]
  (let [note    (@db/all-notes note-id)
        on-blur #(dispatch [:db/edit-note-name
                            {:note-id note-id :name %}])]

    [:div {:style styles/workspace-content}
     [:div {:content-editable true
            :on-blur #(-> % .-target .-textContent on-blur)
            :style (conj styles/h1 styles/content-editable)}
       (note :name)]
      [:div {:dangerouslySetInnerHTML {:__html
        (-> note :content js/marked)}}]]))

(defn home []
  [:div "Let's learn something!"])

(defn workspace []
  [:div {:style styles/workspace}
   (case (first @db/ui-workspace)
     :home  [home]
     ;; TODO: it's not clear what nth is doing here.
     ;:note  [note-workspace  (nth @db/ui-workspace 1)]
     :note  [:<> [topbar/note (nth @db/ui-workspace 1)]
                 [note-workspace (nth @db/ui-workspace 1)]]

     :edit-note [:<> [topbar/editing-note (nth @db/ui-workspace 1)]
                     [edit-note (nth @db/ui-workspace 1)]]

     :deck  [:<> [topbar/deck (nth @db/ui-workspace 1)]
                 [deck-workspace (nth @db/ui-workspace 1)]]

     :edit-deck-template [:<> [topbar/editing-deck-template (nth @db/ui-workspace 1)] 
                              [edit-template (nth @db/ui-workspace 1)]]

     :learn [:<> [topbar/deck (nth @db/ui-workspace 1)]
                 [study/learn (nth @db/ui-workspace 1)]]

     :review [:<> [topbar/deck (nth @db/ui-workspace 1)]
                  [study/review (nth @db/ui-workspace 1)]])])
