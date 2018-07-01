(ns app.views.workspaces.study
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljs-time.core :refer [today]]
            [app.views.ui    :as ui]
            [app.views.util.keyboard :as kbd]
            [app.views.card  :refer [render-card]]
            [app.styles     :as styles]
            [app.db         :as db
            [app.events     :refer [dispatch]]))

;; Start with an ordered list.
;; If you get one right, remove it from the list.
;; If you get one wrong, move it to the back of the list.
(def review-queue [])

(defn foobar [card forgot remembered delete]
  (r/with-let [current-side (r/atom 1)
               first-side?  (r/atom false)
               last-side?   (r/atom false)
               prev-side    #(swap! current-side dec)
               next-side    #(swap! current-side inc)
               handler      #(case (.-which %)
                              kbd/space (if @last-side?
                                           (do (reset! current-side 1)
                                               (remembered))
                                          (next-side))
                              kbd/left-arrow (if-not @first-side? (prev-side))
                              kbd/right-arrow (if-not @last-side? (next-side))
                              kbd/r (let [audio (aget (.getElementsByTagName js/document "audio") 0)]
                                      (aset audio "currentTime" 0)
                                      (.play audio))
                              nil)
               _ (js/document.addEventListener "keydown" handler)]

    (let [deck-id (:deck-id card)
          deck            (@db/all-decks deck-id)
          deck-fields     (r/track db/fields-for-deck deck-id)
          sides (-> @deck :template (str/split #"---"))
          _ (reset! last-side? (= @current-side (count sides)))
          _ (reset! first-side? (= @current-side 1))]

      [:<>
       [render-card card (nth sides (- @current-side 1)) @deck-fields]
       [ui/button "Delete" #(do (reset! current-side 1)
                                (delete))]
       (if @last-side?
         [:<> [ui/button "Forgot" #(do (reset! current-side 1)
                                       (forgot))]
              [ui/button "Remembered" #(do (reset! current-side 1)
                                           (remembered))]]
         [:<> [ui/button "Next" next-side]])])

    (finally (js/document.removeEventListener "keydown" handler))))


(defn review [deck-id]
  (let [cards     (r/track db/cards-for-deck deck-id)
        due-cards (r/track db/to-review @cards)
        due-card  (first @due-cards)]

    [:div {:style styles/workspace-content}
     (if due-card
       [foobar due-card
        #(dispatch [:db/review-card {:card-id (due-card :id) :remembered? false}])
        #(dispatch [:db/review-card {:card-id (due-card :id) :remembered? true}])
        #(dispatch [:db/delete-card (due-card :id)])]
       ;; TODO: Show hint that there are x number of unlearned cards.
       [:div "No Cards to study!"])]))

(defn learn [deck-id]
  ;; Set up review queue here.
  (r/with-let [due-slot (r/atom 0)]

    (let [cards     (r/track db/cards-for-deck deck-id)
          due-cards (r/track db/to-learn @cards)
          _ (if (>= @due-slot (count @due-cards))
                    (reset! due-slot 0))
          due-card (nth @due-cards @due-slot)]

      [:div {:style styles/workspace-content}
       (if due-card
         [foobar due-card
          ;;TODO: Why do these need to be passed in?
          #(swap! due-slot inc)
          #(dispatch [:db/review-card {:card-id (due-card :id) :remembered? true}])
          #(dispatch [:db/delete-card (due-card :id)])]
         [:div "No Cards to learn!"])])))
