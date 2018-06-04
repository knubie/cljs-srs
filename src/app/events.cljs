(ns app.events
  (:require [app.db :refer [state ui-workspace add-record!]]
            [app.models.card :as c]))

;; -- Event Handlers -------------------------------------------------------
;;
;; Event handlers are passed an action, which is a vector of shape
;; [:action-name arg & args]
;; The multi method dispatches on the `first` element of the vector, namely
;; the action-name.

(defmulti  handle first) ;; Tests the first arg.

(defmethod handle :select-deck
  [[_ deck-id]]

  (swap! state assoc-in [:ui :workspace] [:deck deck-id]))


(defmethod handle :set-modal
  [[_ card-id]]

  (swap! state assoc-in [:ui :modal] card-id))


(defmethod handle :reset-modal
  [[_]]

  (swap! state assoc-in [:ui :modal] nil))

(defmethod handle :select-note-type
  [[_ note-type-id]]

  (swap! state assoc-in [:ui :workspace] [:note-type note-type-id]))


(defmethod handle :study
  [[_ deck-id]]

  (reset! ui-workspace [:study deck-id]))


(defmethod handle :learn-card
  [[_ card-id]]

  (swap! state assoc-in [:db :cards card-id :status] :to-review))


(defmethod handle :review-card
  [[_ {:keys [card-id remembered?]}]]

  (swap! state update-in [:db :cards card-id] c/remember))

;(defmethod handle :delete-note [[_ note-id]]
  ;(swap! db-notes dissoc note-id))

(defmethod handle :add-card
  [[_ card]]

  (add-record! :cards card))

(defmethod handle :add-empty-card
  [[_ deck-id fields]]

  (add-record! :cards {:deck-id deck-id
                       :reviews []
                       :fields (into {} (for [field fields] [(field :id) ""]))}))

(defmethod handle :edit-card
  [[_ card]]

  (swap! state assoc-in [:db :cards (card :id)] card))


(defmethod handle :edit-card-field
  [[_ {:keys [card-id field-id field-value]}]]

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

(defn validate-state [] (js/console.log "Validating state."))
(defn save-state [] (js/console.log "Saving state."))

;; TODO: Create UI actions and DB actions.
;; DB Actions persist to local storage, whereas UI actions do not.
(defn dispatch [action]
  (handle action)
  (validate-state)
  (swap! state update-in [:actions] conj action)
  (save-state))
