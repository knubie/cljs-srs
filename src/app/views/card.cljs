(ns app.views.card
  (:require [reagent.core   :as r]
            [app.models.card :refer [formatted-due]]
            [app.db         :refer [state where find-one]]
            [cljstache.core :as stache]))

(def all-cards (r/cursor state [:db :cards]))
(def all-decks (r/cursor state [:db :decks]))
(def all-fields (r/cursor state [:db :fields]))
(def remarkable (js/Remarkable. #js {:html true}))

;; TODO: Rename this?
(defn render-card [card template deck-fields]
  (let [data (into {}
    (for [[id field] deck-fields]
      [(-> field :name keyword)
       (-> card :fields id)]))]

    [:div {:dangerouslySetInnerHTML {:__html
      (.render remarkable (-> template (stache/render data)))}}]))

;; TODO: Rename this?
(defn render-card3 [card-id]
  ;; TODO: Use r/track here
  (let [card (@all-cards card-id)
        deck (@all-decks (card :deck-id))
        template (deck :template)
        deck-fields (->> @all-fields (where :deck-id (card :deck-id)))
        data (into {}
          (for [[id field] deck-fields]
            [(-> field :name keyword)
             (-> card :fields id)]))]

    [:div {:dangerouslySetInnerHTML {:__html
      (.render remarkable (-> template (stache/render data)))}}]))
