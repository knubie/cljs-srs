(ns app.events.interface
  (:require [cljs.spec.alpha :as s]))

;; -- Event Handlers -------------------------------------------------------
;;
;; Event handlers are passed an action, which is a vector of shape
;; [:action-name arg & args]
;; The multi method dispatches on the `first` element of the vector, namely
;; the action-name.
;;
;; All of the methods for `handle` are defined in events.ui and events.db

(defmulti handle first) ;; Tests the first arg.

;; -- Event Action Spec ----------------------------------------------------
;;
;; This is a multi-spec designed to validate the `action` param sent to `handle`

(defmulti event-spec first)
(defmethod event-spec :ui/select-deck [_]
  (s/cat :action-name keyword? :deck-id :app.db/deck-id))
(s/def :app.events/action (s/multi-spec event-spec first))
