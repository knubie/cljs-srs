(ns app.db
  (:require [cljs.spec.alpha :as s]
            [cljs.reader     :as edn]
            [app.sample-note :refer [sample-note]]
            [reagent.core    :as r]
            [cljs-time.core  :as cljs-time]
            [cljs-time.coerce :refer [to-local-date]]
            [cljs-time.instant] ;; Required to serialize data objects as EDN.
            [cljs-time.extend]))

;; -- Spec -----------------------------------------------------------------
;;
;; This spec outlines the "schema" of our application state. Application state
;; only changes through event handlers, so every time an event handler is run,
;; the entire application state is checked through this spec.

(s/def ::id       keyword?)
(s/def ::deck-id  keyword?)
(s/def ::name     string?)
(s/def ::template string?)
(s/def ::due      inst?)
(s/def ::interval number?)
(s/def ::remembered? boolean?)
(s/def ::learning? boolean?)
(s/def ::type #{"text" "image" "audio"})

(s/def ::deck   (s/keys :req-un [::id ::name ::template]))
(s/def ::note   (s/keys :req-un [::id ::name]))
(s/def ::field  (s/keys :req-un [::id ::deck-id ::name ::type]))

;; interval is days
(s/def ::review     (s/keys :req-un [::date ::due ::interval ::remembered?]))
(s/def ::reviews    (s/coll-of ::review :kind vector?))
(s/def :card/fields (s/map-of ::id string?))
(s/def ::card       (s/keys :req-un [::id ::deck-id ::reviews :card/fields ::learning?]
                            :opt-un [::due]))

(s/def ::decks  (s/map-of ::id ::deck))
(s/def ::cards  (s/map-of ::id ::card)) ;; TODO: cannot have due without reviews
(s/def ::notes  (s/map-of ::id ::note))
(s/def ::fields (s/map-of ::id ::field))

(s/def ::db (s/keys :req-un [::decks ::notes ::fields]))


;; -- Initial State --------------------------------------------------------

(defonce state
  (r/atom
    {:actions []
     :ui {:workspace [:home]
          :modal  {:card-id nil
                   :open? false}}
     :db {:notes  {} ;; TODO: Write spec
          :decks  {}
          :fields {}
          :cards  {}}}))

(defonce ui-workspace (r/cursor state [:ui :workspace]))
(defonce all-decks    (r/cursor state [:db :decks]))
(defonce all-notes    (r/cursor state [:db :notes]))
(defonce all-cards    (r/cursor state [:db :notes]))
(defonce all-fields   (r/cursor state [:db :fields]))
(defonce modal        (r/cursor state [:ui :modal]))


;; -- Seed Data ------------------------------------------------------------

(defn add-record! [model attrs]
  (let [id (keyword (str (random-uuid)))]
    (swap! state assoc-in [:db model id] (assoc attrs :id id))
    id))

(defn seed-data []
  (let [my-note
          (add-record! :notes
            {:name "日本語の文法"
             :content sample-note})

        my-deck
          (add-record! :decks
            {:name   "日本語"
             :template "#{{Question}}\n\n---\n\n#{{Answer}}"})

        other-deck
          (add-record! :decks
            {:name   "Sample Deck"
             :template "#{{Front}}\n\n---\n\n#{{Back}}"})

        _
          (add-record! :fields
            {:deck-id other-deck :name "Front" :type "text"})

        _
          (add-record! :fields
            {:deck-id other-deck :name "Back" :type "text"})

        question-field
          (add-record! :fields
            {:deck-id my-deck :name "Question" :type "text"})

        answer-field
          (add-record! :fields
            {:deck-id my-deck :name "Answer" :type "text"})]

    (do
      (add-record! :cards
        {:deck-id my-deck
         :due     (cljs-time/today)
         :learning? false
         :reviews [
           {:date (cljs-time/today)
            :due (cljs-time/today)
            :interval 1
            :remembered? true}
         ]
         :fields  {question-field "こんにちは"
                   answer-field   "Hello"}})

      (add-record! :cards
        {:deck-id my-deck
         :learning? true
         :reviews []
         :fields  {question-field "おはようございます"
                   answer-field   "Good morning"}}))))

;; -- Query Helpers --------------------------------------------------------
;;
;; These are a series of functions that can be used to filter and find specific
;; records in the database.
;;
;; By convention, the `find-one` function return a single result, whereas the
;; `where` methods filter the hash.

;; TODO: Test me!
;; TODO: Accept hash of key matcher pairs
(defn where [key matcher collection]
  (->> collection
       (filter #(-> % second key (= matcher))) ;; `second` is used to grab
       (into {})))                             ;; the record. [:id { .. }]

(defn where-id [keys collection]
  (select-keys collection keys))

(defn find-one [key matcher collection]
  (->> collection vals
       (filter #(-> % key (= matcher)))
       first))

;; TODO: Test me
(defn to-review [collection]
  (->> collection vals
       ;; TODO: make this more efficient, transducers?
       ;; TODO: Test with cards that have nil due
       (remove #(nil? (% :due)))
       (filter #(or (cljs-time/before? (% :due) (cljs-time/today))
                    (cljs-time/equal?  (% :due) (cljs-time/today))))))

;(def to-learn (partial where :learning? true))
(defn to-learn [collection]
  (->> collection (where :learning? true) vals))

;; -- Persistence ----------------------------------------------------------
;;
;; For now the the entire application state is saved to LocalStorage after each
;; event. In the future we might selectively store only certain parts of the
;; state that gets stored. For instance, we may decide to not store the UI state.

(def local-storage-key "cljs-app")

(defn state->local-storage []
  (.setItem js/localStorage local-storage-key (prn-str @state)))

(defn local-storage->state []
  (reset! state (some->> (.getItem js/localStorage local-storage-key)
                          (edn/read-string {:readers {'inst to-local-date}}))))

(defn initialize-db []
  (let [local-storage-state (some->> (.getItem js/localStorage local-storage-key)
                                     (edn/read-string {:readers {'inst to-local-date}}))]
    (if (nil? local-storage-state)
      (seed-data)
      (local-storage->state))))
