(ns app.views.workspace
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljs-time.core :refer [today]]
            [app.views.data-table :refer [data-table]]
            [app.views.ui    :as ui]
            [app.views.card  :refer [render-card]]
            [app.styles     :as styles]
            [app.db         :refer [state ui-workspace where]]
            [app.models.card :as c]
            [app.events     :refer [dispatch]]))

;; -- DB Cursors -----------------------------------------------------------

(def all-fields (r/cursor state [:db :fields]))
(def all-cards (r/cursor state [:db :cards]))

(defn note [note-id]
  (let [note @(r/cursor state [:db :notes note-id])]
    [:div {:dangerouslySetInnerHTML {:__html
      (-> note :content js/marked)}}]))


(defn study [deck-id]
  (r/with-let [current-side (r/atom 1)
               last-side? (r/atom false)
               next-side #(swap! current-side inc)
               remember #(js/console.log "Remember.")
               handler #(case (.-which %) 32 (if @last-side?
                                               (remember)
                                               (next-side))
                                          nil)
               _ (js/document.addEventListener "keydown" handler)]

    (let [due-card (->> @all-cards (where :deck-id deck-id)
                                   (where :due (today)) vals first)
          deck @(r/cursor state [:db :decks deck-id])
          sides (-> deck :template (str/split #"---")) 
          _ (reset! last-side? (= @current-side (count sides)))
          deck-fields (->> @all-fields (where :deck-id deck-id))]

      [:<>
       (if due-card
         [:<>
          (for [side (take @current-side sides)]
            [render-card due-card side deck-fields])

          (if @last-side?
            [:<> [ui/button "Forgot"
                  #(dispatch [:review-card {:card-id (due-card :id)
                                            :remembered? false}])]
                 [ui/button "Remembered"
                  #(dispatch [:review-card {:card-id (due-card :id)
                                            :remembered? true}])]]
            [ui/button "Next" next-side])]
         [:div "No Cards!"])])

    (finally (js/document.removeEventListener "keydown" handler))))


(defn deck [deck-id]
  (let [deck        @(r/cursor state [:db :decks deck-id])
        deck-fields (->> @all-fields (where :deck-id deck-id) vals)
        cards       (->> @all-cards  (where :deck-id deck-id) vals)]

    [:<>
     [:div {:style {:margin-bottom "0.5em"}}
      
      [:div {:content-editable true
             :on-blur #(dispatch [:edit-deck-name {
                                  :deck-id deck-id
                                  :name    (-> % .-target .-textContent)}])
             :style (conj styles/h1
                          {:outline 0
                           :-webkit-user-modify 'read-write-plaintext-only})}
       (deck :name)]]

     [ui/button "Study" #(dispatch [:study deck-id])]
      [data-table deck-fields cards deck-id]]))


(defn home []
  [:div "Let's learn something!"])


(defn workspace []
  [:div {:style styles/workspace}
   [:div {:style styles/workspace-content}
    (case (first @ui-workspace)
      :home  [home]
      ;; TODO: it's not clear what nth is doing here.
      :note  [note  (nth @ui-workspace 1)]
      :deck  [deck  (nth @ui-workspace 1)]
      :study [study (nth @ui-workspace 1)])]])
