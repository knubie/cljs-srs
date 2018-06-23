(ns app.views.workspaces.study
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljs-time.core :refer [today]]
            [app.views.ui    :as ui]
            [app.views.util.keyboard :as kbd]
            [app.views.card  :refer [render-card]]
            [app.styles     :as styles]
            [app.db         :refer [state ui-workspace where to-review to-learn]]
            [app.events     :refer [dispatch]]))

(def all-fields (r/cursor state [:db :fields]))
(def all-cards (r/cursor state [:db :cards]))

;; Start with an ordered list.
;; If you get one right, remove it from the list.
;; If you get one wrong, move it to the back of the list.
(def review-queue [])

(defn foobar [card forgot remembered]
  (r/with-let [current-side (r/atom 1)
               last-side?   (r/atom false)
               next-side    #(swap! current-side inc)
               handler      #(case (.-which %)
                              kbd/space (if @last-side? (remembered) (next-side))
                              nil)
               _ (js/document.addEventListener "keydown" handler)]

    (let [deck @(r/cursor state [:db :decks (card :deck-id)])
          deck-fields (->> @all-fields (where :deck-id (card :deck-id)))
          sides (-> deck :template (str/split #"---"))
          _ (reset! last-side? (= @current-side (count sides)))]

      [:<>
       [:div
        [render-card card (nth sides (- @current-side 1)) deck-fields]]
       (if @last-side?
         [:div [ui/button "Forgot" #(do (reset! current-side 1)
                                        (forgot))]
               [ui/button "Remembered" #(do (reset! current-side 1)
                                            (remembered))]]
         [ui/button "Next" next-side])])

    (finally (js/document.removeEventListener "keydown" handler))))


(defn review [deck-id]
  ;; Set up review queue here.
  (let [due-cards (->> @all-cards (where :deck-id deck-id) to-review)
        due-card (first due-cards)]

    (if due-card
      [foobar due-card
       #(dispatch [:review-card {:card-id (due-card :id) :remembered? false}])
       #(dispatch [:review-card {:card-id (due-card :id) :remembered? true}])]
      ;; TODO: Show hint that there are x number of unlearned cards.
      [:div "No Cards to study!"])))

(defn learn [deck-id]
  ;; Set up review queue here.
  (r/with-let [due-slot (r/atom 0)]

    (let [due-cards (->> @all-cards (where :deck-id deck-id) to-learn)
          _ (if (>= @due-slot (count due-cards))
                    (reset! due-slot 0))
          due-card (nth due-cards @due-slot)]

      (if due-card
        [foobar due-card
         #(do (dispatch [:review-card {:card-id (due-card :id) :remembered? false}])
              (swap! due-slot inc))
         #(dispatch [:review-card {:card-id (due-card :id) :remembered? true}])]
        [:div "No Cards to learn!"]))))
