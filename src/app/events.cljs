(ns app.events
  (:require [cljs.spec.alpha :as s]
            [app.db :refer [state ui-workspace
                            add-record! state->local-storage]]
            [app.models.card :as c]))

;; -- Event Handlers -------------------------------------------------------
;;
;; Event handlers are passed an action, which is a vector of shape
;; [:action-name arg & args]
;; The multi method dispatches on the `first` element of the vector, namely
;; the action-name.

(defmulti  handle first) ;; Tests the first arg.

;; UI ----------------------------------

(defmethod handle :select-deck
  [[_ deck-id]]

  (swap! state assoc-in [:ui :workspace] [:deck deck-id]))


(defmethod handle :ui/select-note
  [[_ note-id]]

  (swap! state assoc-in [:ui :workspace] [:note note-id]))


(defmethod handle :set-modal
  [[_ card-id]]

  (swap! state assoc-in [:ui :modal] {:card-id card-id :open? true}))


(defmethod handle :close-modal
  [[_]]

  (swap! state assoc-in [:ui :modal :open?] false))

(defmethod handle :ui/review
  [[_ deck-id]]

  (reset! ui-workspace [:review deck-id]))


(defmethod handle :ui/learn
  [[_ deck-id]]

  (reset! ui-workspace [:learn deck-id]))

(defmethod handle :ui/edit-deck-template
  [[_ deck-id]]

  (reset! ui-workspace [:edit-deck-template deck-id]))

;; /UI ----------------------------------



(defmethod handle :learn-card
  [[_ card-id]]

  (swap! state assoc-in [:db :cards card-id :status] :to-review))


(defmethod handle :review-card
  [[_ {:keys [card-id remembered?]}]]

  ;; TODO: Check args against a spec.
  (if remembered?
    (swap! state update-in [:db :cards card-id] c/remember)
    (swap! state update-in [:db :cards card-id] c/forget)))

;(defmethod handle :delete-note [[_ note-id]]
  ;(swap! db-notes dissoc note-id))

(defmethod handle :add-card
  [[_ card]]

  (add-record! :cards card))


(defmethod handle :new-note
  [_]

  (add-record! :notes {:name    "New Note"
                       :content "Edit me!"}))


(defmethod handle :new-deck
  [_]

  (let [deck-id (add-record! :decks
    {:name     "New Deck"
     :template "#{{Front}}\n\n---\n\n#{{Back}}"})]

    (add-record! :fields
      {:deck-id deck-id :name "Front" :type "text"})
    (add-record! :fields
      {:deck-id deck-id :name "Back" :type "text"})
    (handle [:select-deck deck-id])))


(defmethod handle :nest-deck
  [[_ child-deck-id parent-deck-id]]
  (swap! state assoc-in [:db :decks child-deck-id :deck-id] parent-deck-id))
  

(defmethod handle :add-empty-card
  [[_ deck-id fields]]

  (add-record! :cards {:deck-id deck-id
                       :reviews []
                       :learning? true
                       ;; TODO: Derive fields
                       :fields (into {} (for [field fields] [(field :id) ""]))}))

;; TODO: Consolidate these two ----------------------

(defmethod handle :edit-deck-name
  [[_ {:keys [deck-id name]}]]

  (swap! state assoc-in [:db :decks deck-id :name] name))


(defmethod handle :edit-deck-template
  [[_ {:keys [deck-id template]}]]

  (swap! state assoc-in [:db :decks deck-id :template] template))

;; TODO: ---------------------------------------------

(defmethod handle :edit-card
  [[_ card]]

  (swap! state assoc-in [:db :cards (card :id)] card))


(defmethod handle :edit-card-field
  [[_ {:keys [card-id field-id field-value]}]]

  (js/console.log (str "Saving: " field-value))
  (swap! state assoc-in [:db :cards card-id :fields field-id] field-value))


(defmethod handle :add-field
  [[_ field]]

  (add-record! :fields field))


(defmethod handle :edit-field
  [[_ field]]

  (swap! state assoc-in [:db :fields (field :id)] field))


;; -- Action Dispatch ------------------------------------------------------
;;
;; The `dispatch` function recieves incoming actions and performs pre-
;; and post- tasks before delegating to the corresponding event handler.


(defn validate-state []
  (when-not (s/valid? :app.db/db (-> @state :db))
    (throw
      (ex-info
        (str "spec check failed: "
             (s/explain-str :app.db/db (-> @state :db))) {}))))

(defn save-state []
  (js/console.log "Saving state.")
  (state->local-storage))

;; TODO: Create UI actions and DB actions.
;; DB Actions persist to local storage, whereas UI actions do not.
(defn dispatch [action]
  (handle action)
  (validate-state)
  (swap! state update-in [:actions] conj action)
  (save-state))
