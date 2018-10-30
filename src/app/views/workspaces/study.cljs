(ns app.views.workspaces.study
  (:require [clojure.string :as str] 
            [reagent.core   :as r]
            [cljs-time.core :refer [today]]
            [app.views.ui    :as ui]
            [app.views.util.keyboard :as kbd]
            [app.views.card  :refer [render-card]]
            [app.styles     :as styles]
            [app.db         :as db]
            [app.events     :refer [dispatch]]))

(def locale
  {:delete {:en "Delete"
            :jp "消去する"}})

(def lang :en)

(defn l [word] (-> locale word lang))

(defn card-quiz [card {:keys [forgot]}]
  (r/with-let [current-side (r/atom 1)
               total-sides  (r/atom 1)
               first-side?  #(= @current-side 1)
               last-side?   #(= @current-side @total-sides)
               first-side   #(reset! current-side 1)
               prev-side    #(if-not (first-side?) (swap! current-side dec))
               next-side    #(if-not (last-side?)  (swap! current-side inc))
               this-card    (r/atom card)
               remember     #(do (first-side)
                                 (dispatch [:db/review-card {:card-id (% :id) :remembered? true}]))
               forget       #(do (first-side)
                                 (forgot %))
               delete       #(do (first-side)
                                 (dispatch [:db/delete-card (% :id)]))
               edit         #(dispatch [:ui/set-modal (% :id)])
               handler      #(case (.-which %)
                              kbd/space (do (.preventDefault %)
                                            (if (last-side?) (remember @this-card) (next-side)))
                              kbd/left-arrow  (prev-side)
                              kbd/right-arrow (next-side)
                              kbd/d           (delete @this-card)
                              kbd/r           (let [audio (aget (.getElementsByTagName js/document "audio") 0)]
                                                (aset audio "currentTime" 0)
                                                (.play audio))
                              nil)
               kbd-handler  #(if-not (@db/modal :open?) (handler %))
               _ (js/document.addEventListener "keydown" kbd-handler)]

    (let [deck-id         (:deck-id card)
          deck            (@db/all-decks deck-id)
          deck-fields     (r/track db/fields-for-deck deck-id)
          sides           (-> deck :template (str/split #"---"))
          _               (reset! total-sides (count sides))
          _               (reset! this-card card)]

      [:<>
       [render-card card (nth sides (- @current-side 1)) @deck-fields]
       [:div {:style styles/study-buttons}
        [ui/button (l :delete)         #(delete card)]
        [ui/button "Edit"              #(edit card)]
        (if (last-side?)
          [:<> [ui/button "Forgot"     #(forget card)]
               [ui/button "Remembered" #(remember card)]]

          [:<> [ui/button "Next"       next-side]])]])

    (finally (js/document.removeEventListener "keydown" kbd-handler))))


(defn review [deck-id]
  (let [cards        (r/track db/cards-for-deck deck-id)
        due-cards    (r/track db/to-review @cards)
        lapsed-cards (r/track db/lapsed @cards)
        all-cards    (concat @due-cards @lapsed-cards)
        due-card     (first all-cards)]

    [:div {:style styles/workspace-content}
     (if due-card
       [card-quiz due-card {:forgot #(dispatch [:db/review-card {:card-id (% :id) :remembered? false}])}]
       ;; TODO: Show hint that there are x number of unlearned cards.
       [:div {:style {:height "100%"
                      :display "flex"
                      :flex-direction "column"
                      :justify-content "center"
                      :text-align "center"}}
        [:div {:style {:font-size "80px"}} "🍣"]
        [:div {:style {:font-size "24px"
                       :font-weight "600"
                       :color "rgb(66, 66, 65)"}} "No more cards left!"]])]))

(defn learn [deck-id]
  ;; Set up review queue here.
  (r/with-let [due-slot (r/atom 0)]

    (let [cards     (r/track db/cards-for-deck deck-id)
          due-cards (r/track db/to-learn @cards)
          _ (if (>= @due-slot (count @due-cards))
                    (reset! due-slot 0))
          due-card (nth @due-cards @due-slot nil)]

      [:div {:style styles/workspace-content}
       (if due-card
         [card-quiz due-card {:forgot #(swap! due-slot inc)}]
         [:div "No Cards to learn!"])])))
