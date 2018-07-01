(ns app.views.workspace
  (:require [clojure.string             :as str] 
            [reagent.core               :as r]
            [cljs-time.core             :refer [today]]
            [app.views.workspaces.study :as study]
            [app.views.workspaces.edit  :refer [edit-template]]
            [app.views.workspaces.deck  :refer [deck-workspace]]
            [app.views.util.keyboard    :as kbd]
            [app.views.card             :refer [render-card]]
            [app.styles                 :as styles]
            [app.db                     :refer [state ui-workspace]]))


;(extend-type js/FileList ISeqable)

;; -- DB Cursors -----------------------------------------------------------

(defn note [note-id]
  (let [note @(r/cursor state [:db :notes note-id])]
    [:div {:dangerouslySetInnerHTML {:__html
      (-> note :content js/marked)}}]))

(defn home []
  [:div "Let's learn something!"])


(defn workspace []
  [:div {:style styles/workspace}
   [:div {:style styles/workspace-content}
    (case (first @ui-workspace)
      :home  [home]
      ;; TODO: it's not clear what nth is doing here.
      :note  [note  (nth @ui-workspace 1)]
      :deck  [deck-workspace  (nth @ui-workspace 1)]
      :edit-deck-template [edit-template (nth @ui-workspace 1)]
      :learn [study/learn (nth @ui-workspace 1)]
      :review [study/review (nth @ui-workspace 1)])]])
