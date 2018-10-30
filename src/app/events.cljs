(ns app.events
  (:require [cljs.spec.alpha :as s]
            [reagent.core    :as r]
            [app.storage     :as storage]
            [app.events.interface :refer [handle]]
            [app.events.ui]
            [app.events.db]
            [app.db :refer [state ui-workspace where
                            actions->storage
                            add-record! state->storage]]
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
    ;(storage/store-text "actions.txt" (-> @state
                                          ;:actions
                                          ;storage/write-transit))
    (js/setTimeout #(actions->storage) 1)
    ;(storage/append-text "actions.txt" (storage/write-transit action))
    ;(r/after-render #(state->storage))
    ;(js/setTimeout #(state->storage) 1)
    ))
