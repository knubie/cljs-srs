(ns app.views.util.import
  (:require [reagent.core :as r]
            [app.db       :as db]
            [clojure.set  :refer [rename-keys]]
            [cljs.reader  :as edn]))

(def fs (js/require "fs"))

(defn import-deck []
  [:input {:name "import"
           :type "file"

           :on-change (fn [e]
             (let [file (-> e .-target .-files (.item 0) .-path)
                   edn (->> file
                            (.readFileSync fs)
                            (.toString)
                            (edn/read-string))
                   user-field-keys (keys (:fields edn))
                   uuid-keys (map #(keyword (str (random-uuid))) user-field-keys)
                   new-field-keys (zipmap user-field-keys uuid-keys)

                   new-deck (db/add-record! :decks
                              {:name   "Imported Deck"
                               :template ""})

                   new-edn (update-in edn [:fields] (fn [fields]
                     (as-> fields f
                       (rename-keys f new-field-keys)

                       (map (fn [[id field]]
    (swap! db/state assoc-in [:db :fields id] (assoc field :id id :deck-id new-deck))
                       ) f)
                     )
                   ))
                   newer-edn (update-in new-edn [:cards] (fn [cards]
                      (map
                        (fn [card]
                          (as-> card c
                            (update-in c [:fields] rename-keys new-field-keys)
                            (assoc c :deck-id new-deck
                                     :learning? true
                                     :reviews [])
                            (db/add-record! :cards c)
                          )
                        )
                      cards)) )

                   ]
               (js/console.log (clj->js newer-edn))

             ))}
  ]
)
