(ns app.views.card
  (:require [reagent.core   :as r]
            [app.models.card :refer [formatted-due]]
            [app.views.data-table :as table]
            [app.db         :as db]
            [cljstache.core :as stache]))

(def remarkable (js/Remarkable. #js {:html true :breaks true}))

;; TODO: Rename this?
(defn render-card [card template deck-fields]
  (let [data (into {}
              (for [field deck-fields]
                [(-> field :name keyword)
                 (-> card :fields ((:id field)))]))
        furigana (fn [s] (.convert js/kuroshiro s #js{:mode "furigana" :to "hiragana"}))]

    [:div {:dangerouslySetInnerHTML {:__html
      (.render remarkable (-> template (stache/render data) 
                                     
                                     ;kuroshiro.convert(_, #js{mode: "furigana", to: "hiragana"});
                                     ))}}]))

;; TODO: Rename this?
(defn render-card3 [card-id]
  ;; TODO: Use r/track here
  (let [card        (@db/all-cards card-id)
        deck-id     (card :deck-id)
        deck        (@db/all-decks deck-id)
        template    (deck :template)
        deck-fields (r/track db/fields-for-deck deck-id)
        data (into {}
          (for [field @deck-fields]
            [(-> field :name keyword)
             (-> card :fields ((field :id)))]))]

    [:<>
     [:div
      (for [field @deck-fields] ^{:key (field :id)}
        [:div {:style {:display "flex"}}
         [:div {:style {:width 160}} (field :name)]
         [table/table-cell field card 300]
         ;[:div (-> card :fields ((field :id)))]
         ]
      )
      ]
     ;[:div {:dangerouslySetInnerHTML {:__html
           ;(.render remarkable (-> template (stache/render data)))}}]
    ]
    ))
