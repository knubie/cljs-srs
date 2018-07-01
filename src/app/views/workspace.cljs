(ns app.views.workspace
  (:require [clojure.string             :as str] 
            [reagent.core               :as r]
            [app.views.workspaces.study :as study]
            [app.views.workspaces.edit  :refer [edit-template]]
            [app.views.workspaces.deck  :refer [deck-workspace]]
            [app.views.topbar           :as topbar]
            [app.styles                 :as styles]
            [app.db                     :as db]))


;(extend-type js/FileList ISeqable)

;; -- DB Cursors -----------------------------------------------------------

(defn note-workspace [note-id]
  (let [note (@db/all-notes note-id)]
    [:div {:style styles/workspace-content}
      [:div {:dangerouslySetInnerHTML {:__html
        (-> note :content js/marked)}}]]))

(defn home []
  [:div "Let's learn something!"])

(defn workspace []
  [:div {:style styles/workspace}
   (case (first @db/ui-workspace)
     :home  [home]
     ;; TODO: it's not clear what nth is doing here.
     :note  [note-workspace  (nth @db/ui-workspace 1)]
     :deck  [:<> [topbar/deck (nth @db/ui-workspace 1)]
                 [deck-workspace (nth @db/ui-workspace 1)]]
     :edit-deck-template [edit-template (nth @db/ui-workspace 1)]
     :learn [:<> [topbar/deck (nth @db/ui-workspace 1)]
                 [study/learn (nth @db/ui-workspace 1)]]
     :review [:<> [topbar/deck (nth @db/ui-workspace 1)]
                  [study/review (nth @db/ui-workspace 1)]])])
