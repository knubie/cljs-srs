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

(defn randomize-keys [the-keys]
  (zipmap the-keys (repeatedly #(keyword (str (random-uuid))))))

(defn transform-file-path [save-path file]
  (js/encodeURI (.join path save-path file)))

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
                   new-field-keys (randomize-keys (keys (:fields edn)))

                   ;; TODO: Dispatch?
                   new-deck (db/add-record! :decks
                              {:name   "Imported Deck"
                               :template ""})

                   save-path (.join path user-data-media (name new-deck))

                   ;; Find these dynamically based on field type.
                   media-keys [:audio :screenshot]

                   transform-card (fn [card]
                          (-> card
                            (update-in [:fields] rename-keys new-field-keys)

                            (update-in [:fields (new-field-keys :audio)] 
                                       #(js/encodeURI (.join path save-path %)))
                            (update-in [:fields (new-field-keys :screenshot)] 
                                       #(js/encodeURI (.join path save-path %)))
                            (assoc :deck-id new-deck
                                   :learning? true
                                   :reviews [])))

                   new-edn (update-in edn [:fields] rename-keys new-field-keys)]

               (doall
                 (map (fn [[id field]]
                        (swap! db/state
                               assoc-in [:db :fields id] (assoc field :id id :deck-id new-deck)))
                      (new-edn :fields)))

               ;; TODO: Sanitize: remove \N
               ;; remove any backslashes?
               (doall (map #(->> % (transform-card) (db/add-record! :cards))
                           (new-edn :cards)))

               ;; Save the current state to disk.
               (reset! db/all-actions [])
               (js/setTimeout #(db/state->storage) 1)
               (js/setTimeout #(db/actions->storage) 1)
             ))}])
