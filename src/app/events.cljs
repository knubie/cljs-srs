(ns app.events
  (:require [cljs.spec.alpha :as s]
            [reagent.core    :as r]
            [app.events.interface :refer [handle]]
            [app.events.ui]
            [app.events.db]
            [app.db :refer [state ui-workspace where
                            add-record! state->local-storage]]
            [app.models.card :as c]))

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
  (state->local-storage)
  (js/console.log "Called state->local-storage")
  )

(defn validate-action [action]
  (when-not (s/valid? :app.events/action action)
    (throw
      (ex-info
        (str "spec check failed: "
             (s/explain-str :app.events/action action)) {}))))

(defn ui-event? [action] (-> action first namespace (= "ui")))

(defn dispatch [action]
  (validate-action action)
  (handle action)
  (validate-state)
  (when-not (ui-event? action)
    (swap! state update-in [:actions] conj action)
    ;(r/after-render #(save-state))
    (js/setTimeout #(save-state) 1)
    ))
