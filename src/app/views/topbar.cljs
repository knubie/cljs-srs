(ns app.views.topbar
  (:require [clojure.string  :as str] 
            [reagent.core    :as r]
            [cljs-time.core  :refer [today]]
            [app.styles      :as styles]
            [app.events      :refer [dispatch]]
            [app.views.icons :as icons]
            [app.views.ui    :as ui]
            [app.db          :as db]))

(defn deck [deck-id]
  (let [deck            (@db/all-decks deck-id)
        cards           (r/track db/cards-for-deck deck-id)
        due-cards       (r/track db/to-review @cards)
        learned-today   (r/track db/learned-today @cards)
        unlearned-cards (r/track db/unlearned-cards @cards)]

  [:div {:style styles/topbar}
   (str (:name deck))
   [:div
    [ui/button [:<> [icons/pencil 14 14 5] "Edit Template"]
               #(dispatch [:ui/edit-deck-template deck-id])]
    [ui/button (str "Review " (count @due-cards))
               #(dispatch [:ui/review deck-id])]
    [ui/button (str "Learn " (count @learned-today)
                    " / " (count @unlearned-cards))
               #(dispatch [:ui/learn deck-id])]
    [ui/button "Delete" #(dispatch [:db/delete-deck deck-id])]]]
  )
)
