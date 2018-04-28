(ns app.core
  (:require [reagent.core :as r]
            [app.styles   :as styles]))

;; -- Schema ------------------------------------------------------------------

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

;; -- Default Seed Data -------------------------------------------------------

(def state (r/atom {
  :actions []
  :ui {
    :workspace [:home]
  }
  :db {
    :note-types  { }
    :field-types { }
    :notes       { }
    :fields      { }
    :card-types  { }
    :sides       { }
    :cards       { }
    :decks       { }
  }
}))

(def db-note-types  (r/cursor state [:db :note-types]))
(def db-field-types (r/cursor state [:db :field-types]))
(def db-notes       (r/cursor state [:db :notes]))
(def db-fields      (r/cursor state [:db :fields]))
(def db-card-types  (r/cursor state [:db :card-types]))
(def db-sides       (r/cursor state [:db :sides]))
(def db-cards       (r/cursor state [:db :cards]))
(def db-decks       (r/cursor state [:db :decks]))
(def ui-workspace   (r/cursor state [:ui :workspace]))

(defn add-deck! [name]
  (let [id (keyword (str (random-uuid)))]
  (swap! state assoc-in [:db :decks id] {:id id :name name})
  id))

;; -- Seed Data ---------------------------------------------------------------

(defn add-record! [model attrs]
  (let [id (keyword (str (random-uuid)))]
  (swap! state assoc-in [:db model id] (assoc attrs :id id))
  id))

(def default-note-type
  (add-record! :note-types { :name "Default Note" }))

(def question-field-type
  (add-record! :field-types {
    :name "Question"
    :type "text" ;; TODO: Validate
    :note-type default-note-type }))

(def answer-field-type
  (add-record! :field-types {
    :name "Answer"
    :type "text" ;; TODO: Validate
    :note-type default-note-type }))

(def note1
  (add-record! :notes { :note-type default-note-type }))
(def note2
  (add-record! :notes { :note-type default-note-type }))

(add-record! :fields {
  :field-type question-field-type
  :note note1
  :value "こんにちは" })

(add-record! :fields {
  :field-type answer-field-type
  :note note1
  :value "Hello" })

(add-record! :fields {
  :field-type question-field-type
  :note note2 
  :value "おはようございます" })

(add-record! :fields {
  :field-type answer-field-type
  :note note2
  :value "Good morning" })

(def default-card-type
  (add-record! :card-types {
    :name "Default Card"
    :note-type default-note-type }))

(def front-side
  (add-record! :sides {
    :name "Front"
    :display "{{Question}}"
    :card-type default-card-type}))

(def back-side
  (add-record! :sides {
    :name "Back"
    :display "{{Answer}}"
    :card-type default-card-type}))

(def deck1 (add-record! :decks { :name "日本語" }))

(add-record! :cards {
  :card-type default-card-type
  :note note1
  :deck deck1
  :status :new
  :last-review "3-10-2018" })

(add-record! :cards {
  :card-type default-card-type
  :note note2
  :deck deck1
  :status :new
  :last-review "3-10-2018" })

;; -- Query Helpers -----------------------------------------------------------
;;
;; These are a series of functions that can be used to filter and find specific
;; records in the database.
;;
;; By convention, the `find` function return a single result, whereas the
;; `where` methods filter the hash.


(defn where [key matcher collection]
  (->> collection
       (filter #(-> % second key (= matcher))) ;; `second` is used to grab
       (into {})))                             ;; the record. [:id { .. }]

(defn where-id [keys collection]
  (select-keys collection keys))

;; TODO: find already exists
(defn find [key matcher collection]
  (->> collection vals
       (filter #(-> % key (= matcher)))
       first))

;; -- Persistence -------------------------------------------------------------
;;
;; For now the the entire application state is saved to LocalStorage after each
;; event. In the future we might selectively store only certain parts of the
;; state that gets stored. For instance, we may decide to not store the UI state.

;(def local-storage-key "cljs-app")

;(defn state->local-storage []
  ;(.setItem js/localStorage local-storage-key (prn-str @state)))

;(defn local-storage->state []
  ;(reset! @state (some->> (.getItem js/localStorage local-storage-key)
                          ;edn/read-string)))
  

;; -- Action Dispatch ---------------------------------------------------------
;;
;; The `dispatch` function recieves incoming actions and performs pre- and post-
;; tasks before delegating to the corresponding event handler.

(defn validate-state [] (js/console.log "Validating state."))
(defn save-state [] (js/console.log "Saving state."))

;; TODO: Create UI actions and DB actions.
;; DB Actions persist to local storage, whereas UI actions do not.
(defn dispatch [action]
  (handle action)
  (swap! state update-in [:actions] conj action)
  (validate-state)
  (save-state))

;; -- Event Handlers ----------------------------------------------------------
;;
;; Event handlers are passed an action, which is a vector of shape
;; [:action-name arg & args]
;; The multi method dispatches on the `first` element of the vector, namely
;; the action-name.

(defmulti  handle first) ;; Tests the first

(defmethod handle :select-deck [[_ deck-id]]
  (swap! state assoc-in [:ui :workspace] [:deck deck-id]))

(defmethod handle :select-note-type [[_ note-type-id]]
  (swap! state assoc-in [:ui :workspace] [:note-type note-type-id]))

(defmethod handle :select-study [[_ deck-id]]
  (reset! ui-workspace [:study deck-id]))

(defmethod handle :learn-card [[_ card-id]]
  (swap! state assoc-in [:db :cards card-id :status] :to-review))

;; -- View Components ---------------------------------------------------------

; TODO: Visual indicator when components reload. Useful for optimizations.
(defn side-bar-deck-item [deck]
  [:div {:on-click #(dispatch [:select-deck (deck :id)])}
    (deck :name)])

(defn side-bar-note-type-item [note-type]
  [:div {:on-click #(dispatch [:select-note-type (note-type :id)])}
    (note-type :name)])

(defn side-bar []
  (let [decks      (-> @db-decks      vals)
        note-types (-> @db-note-types vals)]

  [:div {:style styles/side-bar}
    [:h4 "Notes"]
    (for [_ note-types] ^{:key _} [side-bar-note-type-item _])

    [:h4 "Decks"]
    (for [_ decks] ^{:key _} [side-bar-deck-item _])]))

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
  (let [note-type  (->> @db-note-types note-type-id)]

  [:div {:style styles/workspace-content} 
    [:p "Let's look at some notes for " (note-type :name) "!"]]))

;; TODO: skip card and come back
;; TODO: Option to draw on the "front" side of a card

(defn workspace-study [deck-id]
  (let [deck  (->> @db-decks deck-id)
        ;; TODO: Filter by due today.
        cards (->> @db-cards (where :deck deck-id)
                             (where :status :new) vals)]

  [:div {:style styles/workspace-content} 
    (if (= 0 (count cards))
      [:div "All done!"]
      [:div
        [:p "Now studying " (deck :name) "!"]
        [:p "Studying " (count cards) " cards."]
        [study-card (first cards)]
      ]
    )
  ]))

(defn study-card [card]
  (let [current-side (r/atom 0)]

  (fn [card]
    (let [card-type   (->> @db-card-types ((card :card-type)))
          fields      (->> @db-fields (where :note (card :note)))
          sides       (->> @db-sides (where :card-type (card :card-type)) vals)
          last-slide? (= (inc @current-side) (count sides))]

    [:div
      [:input {:type "button"
               :value "Next"
               :on-click (if last-slide?
                           #(do (dispatch [:learn-card (card :id)])
                                (reset! current-side 0))
                           #(swap! current-side inc)) } ]
      [:div "Card: " (card :id)]
      [:div "Study Card - Side: " (-> sides (nth @current-side) :display)]]))))


(defn workspace-deck [deck-id]
  (let [deck  (->> @db-decks deck-id)
        cards (->> @db-cards (where :deck deck-id))]

  [:div {:style styles/workspace-content} 
    [:input {:type "button"
             :value "Study"
             :on-click #(dispatch [:select-study deck-id])}]
    [:h2 (deck :name)]
    [card-table cards] ]))

(defn card-table [cards]
  (let [table-columns [:preview :note-type :card-type :status :due]]

  (js/console.log cards)

  [:table
    [:thead [:tr (for [_ table-columns] [:th _])]]
    [:tbody
      (for [[id card] cards]
        ^{:key id} [card-table-row table-columns card]) ]]))
        ;^{:key id} [:p id]) ]]))

(defn card-table-row [visible-columns card]
  (let [card-type      (->> @db-card-types ((card :card-type)))
        note           (->> @db-notes      ((card :note)))
        note-type      (->> @db-note-types ((note :note-type)))
        fields         (->> @db-fields      (where :note (card :note)))
        field-types    (->> @db-field-types (where :note-type (note :note-type)))
        ;; TODO: This is broken.
        ;field-types    (->> @db-field-types (where-id (note-type :field-types)))
        ;{front-id :id} (->> field-types     (find :name "Front"))
        ;front-field    (->> fields          (find :field-type front-id))
        ]
  
  [:tr
    (for [col visible-columns]
      [:td {:style styles/table-td}
        (case col
          ; TODO: Maybe just render the "Front" side instead.
          ;:preview   (front-field :value)
          :preview   "Not Preview"
          :note-type (note-type :name)
          :card-type (card-type :name)
          :status    (card :status)
          :due       (card :last-review))])]))

(defn app []
  [:div {:style styles/app}
    [side-bar]
    [workspace @ui-workspace]])

(r/render [app] (js/document.getElementById "app"))

