(ns app.core
  (:require [reagent.core :as r]
            [app.styles   :as styles]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;  SCHEMA
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; note-type
;   fields: [field-type]
; field-type
;   name: string
;   type: :text | :image | :audio
; note
;   belongs-to note-type
;   has-many   fields
; field
;   belongs-to: field-type
;   value
; card-type
;   belongs-to note
;   has-many   sides
; side
;   name: string
;   display: mustache HTML
; card
;   card-type
;   status: :new | :learning | :to-review

;(random-uuid)
(def state (r/atom {
  :actions []
  :ui {
    :workspace [:home]
  }
  :db {
    :note-types {
      :nt1 {:id :nt1 :name "Default Note" :field-types [:ft1 :ft2]}
    }
    :field-types {
      :ft1 {:id :ft1 :name "Front" :note-type :nt1 :type "text" }
      :ft2 {:id :ft1 :name "Back" :note-type :nt1 :type "text" }
    }
    :notes {
      :n1 {:id :n1 :note-type :nt1 :fields [:f1 :f2]}
      :n2 {:id :n2 :note-type :nt1 :fields [:f3 :f4]}
    }
    :fields {
      :f1 {:id :f1 :field-type :ft1 :note :n1 :value "こんにちは"}
      :f2 {:id :f2 :field-type :ft2 :note :n1 :value "Hello"}

      :f3 {:id :f3 :field-type :ft1 :note :n2 :value "おはようございます"}
      :f4 {:id :f4 :field-type :ft2 :note :n2 :value "Good morning"}
    }
    :card-types {
      :ct1 {:id :ct1 :name "Default Card" :note-type :nt1 :sides [:s1 :s2]}
    }
    :sides {
      :s1 {:id :s1 :name "Front" :display "{{Front}}"}
      :s2 {:id :s2 :name "Back" :display "{{Back}}"}
    }
    :cards {
      :c1 {:id :c1 :card-type :ct1 :note :n1 :deck :d1 :status :new :last-review "3-10-2018"}
      :c2 {:id :c2 :card-type :ct1 :note :n2 :deck :d1 :status :new :last-review "3-10-2018"}
      :c3 {:id :c3 :card-type :ct1 :note :n1 :deck :d2 :status :new :last-review "4-15-2018"}
    }
    :decks {
      :d1 {:id :d1 :name "日本語"}
      :d2 {:id :d2 :name "foobar"}
    }
  }
}))

;; Models
(defprotocol HasManyCards
  (cards [self] "Returns associated cards."))

(defrecord Deck [id name]

  HasManyCards
  (cards [self]
    (->> @state :db :cards (where :deck (:id self)))))

;; Queries

(defn add-deck! [name]
  (let [id (keyword (str (random-uuid)))]
  (swap! state assoc-in [:db :decks id] {:id id :name name})
  id))

(add-deck! "My new deck")

;; Useful for iterators
(defn get-record [[id record]] record)
(defn get-id     [[id record]] id)

(defn where [key matcher collection]
  (->> collection
       (filter #(-> % get-record key (= matcher)))
       (into {})))

(defn find [key matcher collection]
  (->> collection
       (filter #(-> % get-record key (= matcher)))
       first))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;  ACTION DISPATCH
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn save-state [] (js/console.log "Saving state."))

;; TODO: Create UI actions and DB actions.
;; DB Actions persist to local storage, whereas UI actions do not.
(defn dispatch [action]
  (swap! state update-in [:actions] conj action)
  (handle action)
  (save-state))

(defn action-name [action] (first action))

(defmulti  handle action-name)

(defmethod handle :select-deck [[_ deck-id]]
  (swap! state assoc-in [:ui :workspace] [:deck deck-id]))

(defmethod handle :select-note-type [[_ note-type-id]]
  (swap! state assoc-in [:ui :workspace] [:note-type note-type-id]))

(defmethod handle :select-study [[_ deck-id]]
  (swap! state assoc-in [:ui :workspace] [:study deck-id]))

(defmethod handle :learn-card [[_ card-id]]
  (swap! state assoc-in [:db :cards card-id :status] :to-review))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;  COMPONENTS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn side-bar-deck-item [deck-id]
  [:div {:on-click #(dispatch [:select-deck deck-id])}
    (-> @state :db :decks deck-id :name)])

(defn side-bar-note-type-item [note-type-id]
  [:div {:on-click #(dispatch [:select-note-type note-type-id])}
    (-> @state :db :note-types note-type-id :name)])

(defn side-bar []
  (let [all-deck-ids      (-> @state :db :decks      keys)
        all-note-type-ids (-> @state :db :note-types keys)]

  [:div {:style styles/side-bar}
    [:h4 "Notes"]
    (for [_ all-note-type-ids] ^{:key _} [side-bar-note-type-item _])

    [:h4 "Decks"]
    (for [_ all-deck-ids] ^{:key _} [side-bar-deck-item _])]))

(defn workspace [[workspace-name & args]]
  [:div {:style styles/workspace}
    (case workspace-name
      :home      [workspace-home]
      :note-type [workspace-note-type (first args)]
      :study     [workspace-study     (first args)]
      :deck      [workspace-deck      (first args)])])

(defn workspace-home []
  [:div {:style styles/workspace-content}
    "Welcome to the workspace!"])

(defn workspace-note-type [note-type-id]
  (let [note-type  (->> @state :db :note-types note-type-id)]

  [:div {:style styles/workspace-content} 
    [:p "Let's look at some notes for " (note-type :name) "!"]]))

;; TODO: skip card and come back
;; TODO: Option to draw on the "front" side of a card

(defn workspace-study [deck-id]
  (let [deck  (->> @state :db :decks deck-id)
        ;; TODO: Filter by due today.
        ;; TODO: ✔︎ Filter by new.
        cards (->> @state :db :cards (where :deck deck-id)
                                     (where :status :new))]

  (js/console.log "Re-render workspace-study")

  [:div {:style styles/workspace-content} 
    (if (= 0 (count cards))
      [:div "All done!"]
      [:div
        [:p "Now studying " (deck :name) "!"]
        [:p "Studying " (count cards) " cards."]
        [:p "First card ID " (-> cards vals first :id)]
        [study-card (first cards)]
      ]
    )
  ]))

(defn study-card [[card-id card]]
  (let [current-side (r/atom 0)]
    (fn [[card-id card]]
      (let [card-type   (->> @state :db :card-types ((card :card-type)))
            fields      (->> @state :db :fields (where :note (card :note)))
            sides       (->  @state :db :sides (select-keys (card-type :sides)))
            last-slide? (= (inc @current-side) (count sides))]

      [:div
        [:input {:type "button"
                 :value "Next"
                 :on-click (if last-slide?
                             #(do (dispatch [:learn-card card-id])
                                  (reset! current-side 0))
                             #(swap! current-side inc)) } ]
        [:div "Card: " (card :id)]
        [:div "Study Card - Side: " (-> sides vals (nth @current-side) :display)]
      ]
      )
    )
  )
)


(defn workspace-deck [deck-id]
  (let [deck  (->> @state :db :decks deck-id)
        cards (->> @state :db :cards (where :deck deck-id))]
  [:div {:style styles/workspace-content} 
    [:input {:type "button"
             :value "Study"
             :on-click #(dispatch [:select-study deck-id])}]
    [:h2 (deck :name)]
    [card-table cards] ]))

;; Wishlist
;; (columns % :th)
;; @state -> (~> :db :card-types (card :card-types))

(defn card-table [cards]
  (let [table-columns [:preview :note-type :card-type :status :due]]

  [:table
    [:thead [:tr (for [_ table-columns] [:th _])]]
    [:tbody
      (for [[id card] cards]
        ^{:key id} [card-table-row table-columns card]) ]]))

(defn card-table-row [visible-columns card]
  (let [db              (@state :db)
        card-type       (->> db :card-types ((card :card-type)))
        note            (->> db :notes      ((card :note)))
        note-type       (->> db :note-types ((note :note-type)))
        fields          (->> db :fields      (where :note (card :note)))
        ;field-types     (->> db :field-types (where :note-type (note :note-type)))
        field-types     (-> db :field-types
                            (select-keys (note-type :field-types)))
        [front-id _]    (->> field-types (find :name "Front"))
        [_ front-field] (->> fields (find :field-type front-id))]
  
  [:tr
    (for [col visible-columns]
      [:td {:style styles/table-td}
           (case col
             :preview   (front-field :value)
             :note-type (note-type :name)
             :card-type (card-type :name)
             :status    (card :status)
             :due       (card :last-review))])]))

(defn app []
  [:div {:style styles/app}
    [side-bar]
    [workspace (-> @state :ui :workspace)]])

(r/render [app]
          (js/document.getElementById "app"))

;(.render js/ReactDOM
  ;(.createElement js/React "h2" nil "Hello, React!")
  ;(.getElementById js/document "app"))
