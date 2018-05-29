(ns app.views.side-bar
  (:require [reagent.core    :as r]
            [app.events      :refer [dispatch]]
            [app.styles      :as styles]))

(defn side-bar-deck-item [deck]
  [:div {:on-click #(dispatch [:select-deck (deck :id)])}
    (deck :name)])

(defn side-bar [{decks :decks}]
  [:div {:style styles/side-bar}
    [:h4 "Decks"]
    (for [deck decks] ^{:key (deck :id)}
      [side-bar-deck-item deck])])
