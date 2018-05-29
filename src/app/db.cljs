(ns app.db
  (:require [cljs.spec.alpha :as s]
            [reagent.core    :as r]
            [cljs-time.core  :as cljs-time]
            [cljs-time.extend]))

;; -- Schema ---------------------------------------------------------------

(s/def ::id      keyword?)
(s/def ::deck-id keyword?)
(s/def ::name    string?)
(s/def ::display string?)
(s/def ::reviews vector?)
(s/def ::due     inst?)
(s/def ::type (s/or :text  (= 'text)
                    :image (= 'image)
                    :audio (= 'audio)))

(s/def ::deck   (s/keys :req-un [::id ::name ::display]))
(s/def ::field  (s/keys :req-un [::id ::deck-id ::name ::type]))
(s/def ::fields (s/map-of ::id ::field))
(s/def ::card   (s/keys :req-un [::id ::deck-id ::reviews ::fields]
                        :opt-un [::due]))

; decks
;   :parent-deck-id
;   :name String
;   :fields {id {:name String :type Type}}
;   :card-display String
;
; cards
;   :deck-id DeckID
;   :reviews
;   :next-review date
;   :fields {field-id Value}
; reviews
;   :date
;   :remembered?

;; -- Initial State --------------------------------------------------------

(defonce state
  (r/atom
    {:actions []
     :ui {:workspace [:home]}
     :db {:decks  {}
          :fields {}
          :cards  {}}}))

(defonce ui-workspace   (r/cursor state [:ui :workspace]))

;; -- Util -----------------------------------------------------------------

;; TODO: Move this somewhere else.
(defn today [] (cljs-time/at-midnight (cljs-time/now)))

;; -- Seed Data ------------------------------------------------------------

(defn add-record! [model attrs]
  (let [id (keyword (str (random-uuid)))]
    (swap! state assoc-in [:db model id] (assoc attrs :id id))
    id))

(defonce seed-data
  (let [my-deck
          (add-record! :decks
            {:name   "日本語"
             :display "#{{Question}}\n\n---\n\n#{{Answer}}"})

        question-field
          (add-record! :fields
            {:deck-id my-deck :name "Question" :type "text"})

        answer-field
          (add-record! :fields
            {:deck-id my-deck :name "Answer" :type "text"})]

    (do
      (add-record! :cards
        {:deck-id my-deck
         :due     (today)
         :reviews []
         :fields  {question-field "こんにちは"
                   answer-field   "Hello"}})

      (add-record! :cards
        {:deck-id my-deck
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

;; -- Persistence ----------------------------------------------------------
;;
;; For now the the entire application state is saved to LocalStorage after each
;; event. In the future we might selectively store only certain parts of the
;; state that gets stored. For instance, we may decide to not store the UI state.

;(def local-storage-key "cljs-app")

;(defn state->local-storage []
  ;(.setItem js/localStorage local-storage-key (prn-str @state)))

;(defn local-storage->state []
  ;(reset! @state (some->> (.getItem js/localStorage local-storage-key)
                          ;edn/read-string)))
