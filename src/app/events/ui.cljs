(ns app.events.ui
  (:require [cljs.spec.alpha :as s] 
            [app.events.interface :refer [handle event-spec]]
            [app.db :as db]))

(defmethod event-spec :ui/select-deck [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :ui/select-deck
  [[_ deck-id]]

  (swap! db/state assoc-in [:ui :workspace] [:deck deck-id]))


(defmethod event-spec :ui/select-note [_]
  (s/cat :action-name keyword? :note-id :app.db/id))
(defmethod handle :ui/select-note
  [[_ note-id]]

  (swap! db/state assoc-in [:ui :workspace] [:note note-id]))


(defmethod event-spec :ui/set-modal [_]
  (s/cat :action-name keyword? :card-id :app.db/id))
(defmethod handle :ui/set-modal
  [[_ card-id]]

  (swap! db/state assoc-in [:ui :modal] {:card-id card-id :open? true}))


(defmethod event-spec :ui/close-modal [_]
  (s/cat :action-name keyword?))
(defmethod handle :ui/close-modal
  [[_]]

  ;; FIXME: I think we need to clear out the card here.
  ;; Because autoplay audio keeps playing.
  (swap! db/state assoc-in [:ui :modal :open?] false))


(defmethod event-spec :ui/review [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :ui/review
  [[_ deck-id]]

  (reset! db/ui-workspace [:review deck-id]))


(defmethod event-spec :ui/learn [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :ui/learn
  [[_ deck-id]]

  (reset! db/ui-workspace [:learn deck-id]))


(defmethod event-spec :ui/edit-deck-template [_]
  (s/cat :action-name keyword? :deck-id :app.db/id))
(defmethod handle :ui/edit-deck-template
  [[_ deck-id]]

  (reset! db/ui-workspace [:edit-deck-template deck-id]))
