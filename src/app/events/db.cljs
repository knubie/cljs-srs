(ns app.events.db
  (:require [cljs.spec.alpha :as s]
            [app.events.interface :refer [handle event-spec]]
            [app.db :as db]
            [app.models.card :as c]))


(s/def ::card-id     keyword?)
(s/def ::note-id     keyword?)
(s/def ::field-id    keyword?)
(s/def ::field-value string?)

(defmethod event-spec :db/review-card [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [::card-id :app.db/remembered?])))
(defmethod handle :db/review-card
  [[_ {:keys [card-id remembered?]}]]

  (if remembered?
    (swap! db/state update-in [:db :cards card-id] c/remember)
    (swap! db/state update-in [:db :cards card-id] c/forget)))


(defmethod event-spec :db/delete-card [_]
  (s/cat :action-name keyword? :card-id :app.db/id))
(defmethod handle :db/delete-card
  [[_ card-id]]

  (swap! db/state update-in [:db :cards] dissoc card-id))


(defmethod event-spec :db/new-note [_]
  (s/cat :action-name keyword?))
(defmethod handle :db/new-note
  [_]

  (db/add-record! :notes {:name    "New Note"
                       :content "Edit me!"}))


(defmethod event-spec :db/trash-deck [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :db/trash-deck
  [[_ deck-id]]

  (swap! db/state assoc-in [:db :decks deck-id :trashed?] true))



(defmethod event-spec :db/delete-deck [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :db/delete-deck
  [[_ deck-id]]

  (reset! db/ui-workspace [:home])
  (swap! db/state update-in [:db :decks] dissoc deck-id)
  (->> @db/state :db :decks (db/where :deck-id deck-id) (map (fn [d]
    (swap! db/state update-in [:db :decks] dissoc (d :id)))))
  (->> @db/state :db :fields (db/where :deck-id deck-id) (map (fn [d]
    (swap! db/state update-in [:db :fields] dissoc (d :id)))))
  (->> @db/state :db :cards (db/where :deck-id deck-id) (map (fn [d]
    (swap! db/state update-in [:db :cards] dissoc (d :id)))))
  )


(defmethod event-spec :db/new-deck [_]
  (s/cat :action-name keyword?))
(defmethod handle :db/new-deck
  [_]

  (let [deck-id (db/add-record! :decks
    {:name     "New Deck"
     :template "# {{Front}}\n\n---\n\n# {{Back}}"})]

    (db/add-record! :fields
      {:deck-id deck-id :name "Front" :type "text"})
    (db/add-record! :fields
      {:deck-id deck-id :name "Back" :type "text"})
    (handle [:ui/select-deck deck-id])))


(defmethod event-spec :db/nest-deck [_]
  (s/cat :action-name    keyword?
         :child-deck-id  :app.db/id
         :parent-deck-id :app.db/id))
(defmethod handle :db/nest-deck
  [[_ child-deck-id parent-deck-id]]
  (swap! db/state assoc-in [:db :decks child-deck-id :deck-id] parent-deck-id))
  

(defmethod event-spec :db/add-empty-card [_]
  (s/cat :action-name    keyword?
         :child-deck-id  :app.db/id
         :fields         (s/coll-of :app.db/field)))
(defmethod handle :db/add-empty-card
  [[_ deck-id fields]]
  (let [deck-cards (db/cards-for-deck deck-id)
        next-sort (->> deck-cards (sort-by :sort) last :sort ((fnil inc 1)))]

    (db/add-record! :cards {:deck-id deck-id
                         :sort next-sort
                         :reviews []
                         :learning? true
                         ;; TODO: Derive fields
                         :fields (into {} (for [field fields] [(field :id) ""]))})))

;; TODO: Consolidate these two ----------------------

(defmethod event-spec :db/edit-deck-name [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [:app.db/deck-id :app.db/name])))
(defmethod handle :db/edit-deck-name
  [[_ {:keys [deck-id name]}]]

  (swap! db/state assoc-in [:db :decks deck-id :name] name))


(defmethod event-spec :db/edit-deck-template [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [:app.db/deck-id :app.db/template])))
(defmethod handle :db/edit-deck-template
  [[_ {:keys [deck-id template]}]]

  (swap! db/state assoc-in [:db :decks deck-id :template] template))


(defmethod event-spec :db/edit-note-name [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [:app.db/note-id :app.db/name])))
(defmethod handle :db/edit-note-name
  [[_ {:keys [note-id name]}]]

  (swap! db/state assoc-in [:db :notes note-id :name] name))


(defmethod event-spec :db/edit-note [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [::note-id :app.db/content])))
(defmethod handle :db/edit-note
  [[_ {:keys [note-id content]}]]

  (swap! db/state assoc-in [:db :notes note-id :content] content))

;; TODO: ---------------------------------------------

(defmethod event-spec :db/edit-card [_]
  (s/cat :action-name keyword? :card :app.db/card))
(defmethod handle :db/edit-card
  [[_ card]]

  (swap! db/state assoc-in [:db :cards (card :id)] card))


(defmethod event-spec :db/edit-card-field [_]
  (s/cat :action-name keyword?
         :params (s/keys :req-un [::card-id ::field-id ::field-value])))
(defmethod handle :db/edit-card-field
  [[_ {:keys [card-id field-id field-value]}]]

  (swap! db/state assoc-in [:db :cards card-id :fields field-id] field-value))


(defmethod event-spec :db/add-field [_]
  (s/cat :action-name keyword?
         :field (s/keys :req-un [:app.db/deck-id :app.db/name :app.db/type])))
(defmethod handle :db/add-field
  [[_ field]]

  (db/add-record! :fields field))


(defmethod event-spec :db/edit-field [_]
  (s/cat :action-name keyword? :field :app.db/field))
(defmethod handle :db/edit-field
  [[_ field]]

  (swap! db/state assoc-in [:db :fields (field :id)] field))
