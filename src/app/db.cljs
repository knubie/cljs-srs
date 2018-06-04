(ns app.db
  (:require [cljs.spec.alpha :as s]
            [reagent.core    :as r]
            [cljs-time.core  :as cljs-time]
            [cljs-time.extend]))

;; -- Schema ---------------------------------------------------------------

(s/def ::id      keyword?)
(s/def ::deck-id keyword?)
(s/def ::name    string?)
(s/def ::template string?)
(s/def ::reviews vector?)
(s/def ::due     inst?)
(s/def ::type (s/or :text  (= 'text)
                    :image (= 'image)
                    :audio (= 'audio)))

(s/def ::deck   (s/keys :req-un [::id ::name ::template]))
(s/def ::field  (s/keys :req-un [::id ::deck-id ::name ::type]))
(s/def ::fields (s/map-of ::id ::field))
(s/def ::card   (s/keys :req-un [::id ::deck-id ::reviews ::fields]
                        :opt-un [::due]))

; NOTE: If automatically adjusting the interval modifier, we should keep track
; of which reviews have which interval modifier, so that we have enough sample
; data to adjust the interval modifier again.
;
; NOTE: A lapse should reduce the ease by 20 percentage points.

; decks
;   :parent-deck-id
;   :name String
;   :fields {id {:name String :type Type}}
;   :card-template String
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
     :ui {:workspace [:home]
          :modal nil}
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

;(def deck-id (-> random-uuid str keyword))
;(def f1-id (-> random-uuid str keyword))
;(def f2-id (-> random-uuid str keyword))
;(def c1-id (-> random-uuid str keyword))
;(def c2-id (-> random-uuid str keyword))

;(def seed-data
  ;{:name    "日本語"

   ;:template "#{{Question}}\n\n---\n\n#{{Answer}}"

   ;:fields  {f1-id {:name "Question" :type 'text}
             ;f2-id {:name "Answer"   :type 'text}}

   ;:cards   {c1-id {:due (today)
                    ;:reviews []
                    ;:fields {f1-id "こんにちは"
                             ;f2-id "Hello"}}
             ;c2-id {:reviews []
                    ;:fields {f1-id "おはようございます"
                             ;f2-id "Good morning"}}}})

;(defonce init-db (do
  ;(swap! state assoc-in [:db :decks deck-id]
    ;(-> seed-data (assoc :id deck-id)))))

(defonce seed-data
  (let [my-note
          (add-record! :notes
            {:name "日本語の文法"})

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
