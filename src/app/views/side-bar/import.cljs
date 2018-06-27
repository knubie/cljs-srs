(ns app.views.util.import
  (:require [reagent.core :as r]
            [app.db       :as db]
            [clojure.set  :refer [rename-keys]]
            [cljs.reader  :as edn]))

(def electron (js/require "electron"))
(def fs (js/require "fs"))
(def path (js/require "path"))

(def user-data (-> electron .-remote .-app (.getPath "userData")))
(def user-data-media (.join path user-data "media"))

;; TODO: Run this through the spec
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

                   save-path (.join path user-data-media (name new-deck))

                   media-keys [:audio :screenshot]

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
                          (js/console.log save-path)
                          (as-> card c
                            (update-in c [:fields] rename-keys new-field-keys)

                            (update-in c [:fields (new-field-keys :audio)] 
                                       #(js/encodeURI (.join path save-path %)))
                            (update-in c [:fields (new-field-keys :screenshot)] 
                                       #(js/encodeURI (.join path save-path %)))
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
