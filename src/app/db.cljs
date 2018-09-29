(ns app.db
  (:require [cljs.spec.alpha :as s]
            [cljs.reader     :as edn]
            [app.sample-note :as sample-note]
            [app.storage     :as storage]
            [reagent.core    :as r]
            [cognitect.transit :as transit]
            [cljs-time.core  :as cljs-time]
            [cljs-time.coerce :refer [to-local-date]]
            [cljs-time.instant] ;; Required to serialize date objects as EDN.
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
(s/def ::content  string?)
(s/def ::due      inst?)
(s/def ::interval number?)
(s/def ::remembered? boolean?)
(s/def ::learning? boolean?)
(s/def ::type #{"text" "image" "audio"})

(s/def ::deck   (s/keys :req-un [::id ::name ::template]
                        :opt-un [::deck-id ::trashed?]))
(s/def ::note   (s/keys :req-un [::id ::name ::content]))
(s/def ::field  (s/keys :req-un [::id ::deck-id ::name ::type]))

;; interval is days
(s/def ::review     (s/keys :req-un [::date ::due ::interval ::remembered?]))
(s/def ::reviews    (s/coll-of ::review :kind vector?))
(s/def :card/fields (s/map-of ::id string?)) ;; TODO: weird namespace
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

(def init-state
  {:actions []
   :ui {:workspace [:home]
        :modal  {:card-id nil
                 :open? false}}
   :db {:notes  {} ;; TODO: Write spec
        :decks  {}
        :fields {}
        :cards  {}}})

(defonce ui-workspace (r/cursor state [:ui :workspace]))
(defonce all-decks    (r/cursor state [:db :decks]))
(defonce all-notes    (r/cursor state [:db :notes]))
(defonce all-cards    (r/cursor state [:db :cards]))
(defonce all-fields   (r/cursor state [:db :fields]))
(defonce modal        (r/cursor state [:ui :modal]))


;; -- Seed Data ------------------------------------------------------------

(defn add-record! [model attrs]
  (let [id (keyword (str (random-uuid)))]
    (swap! state assoc-in [:db model id] (assoc attrs :id id))
    id))

(defn seed-data []
  (let [welcome-note
          (add-record! :notes
            {:name "Welcome to Memo!"
             :content sample-note/welcome})

        my-note
          (add-record! :notes
            {:name "日本語の文法"
             :content sample-note/japanese})

        my-deck
          (add-record! :decks
            {:name   "日本語"
             :template "# {{Japanese}}\n---\n# {{Japanese}}\n# {{English}}"})

        sub-deck
          (add-record! :decks
            {:name "アニメ"
             :deck-id my-deck
             :template "# {{Japanese}}\n---\n# {{Japanese}}\n# {{English}}"})

        other-deck
          (add-record! :decks
            {:name   "Sample Deck"
             :template "# {{Front}}\n---\n# {{Front}}\n# {{Back}}"})

        _
          (add-record! :fields
            {:deck-id other-deck :name "Front" :type "text"})

        _
          (add-record! :fields
            {:deck-id other-deck :name "Back" :type "text"})

        _
          (add-record! :fields
            {:deck-id sub-deck :name "Japanese" :type "text"})

        _
          (add-record! :fields
            {:deck-id sub-deck :name "English" :type "text"})

        question-field
          (add-record! :fields
            {:deck-id my-deck :name "Japanese" :type "text"})

        answer-field
          (add-record! :fields
            {:deck-id my-deck :name "English" :type "text"})]

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
                   answer-field   "Good morning"}})
        
      (swap! state assoc-in [:ui :workspace] [:note welcome-note]))))

;; -- Query Helpers --------------------------------------------------------
;;
;; These are a series of functions that can be used to filter and find specific
;; records in the database.
;;
;; By convention, the `find-one` function return a single result, whereas the
;; `where` methods filter the hash.

;; TODO: Test me!
;; TODO: Accept hash of key matcher pairs
;; TODO: Spec the input (map only)
(defn where [key matcher collection]
  (->> collection
       (filter #(-> % second key (= matcher))) ;; `second` is used to grab
       (into {})))                             ;; the record. [:id { .. }]

;(defn wherein [key matcher collection]
  ;(let [matcher->fn #(eval (concat matcher (cons % '())))]

    ;(->> collection
         ;(filter #(-> % second key matcher->fn)) ;; `second` is used to grab
         ;(into {}))))                            ;; the record. [:id { .. }]

(defn where-id [keys collection]
  (select-keys collection keys))

(defn find-one [key matcher collection]
  (->> collection
       (#(if (map? %) (vals %) %))
       (filter #(-> % key (= matcher)))
       first))

;; TODO: Test me
;; TODO: Spec the input (map only)
(defn to-review [collection]
  (->> collection
       (#(if (map? %) (vals %) %))
       ;; TODO: make this more efficient, transducers?
       ;; TODO: Test with cards that have nil due
       (remove #(nil? (% :due)))
       (filter #(or (cljs-time/before? (% :due) (cljs-time/today))
                    (cljs-time/equal?  (% :due) (cljs-time/today))))))

(defn lapsed [collection]
  (->> collection
       (#(if (map? %) (vals %) %))
       (filter #(and (-> % :reviews not-empty)
                     (-> % :learning?)))))

(defn to-learn [cards]
  (->> cards (filter :learning?) (sort-by :sort)))


(defn child-decks-for-deck [deck-id]
  (->> @all-decks (where :deck-id deck-id) vals))


(defn fields-for-deck [deck-id]
  (->> @all-fields (where :deck-id deck-id) vals))


(defn cards-for-deck [deck-id]
  (let [all-deck-ids
        (loop [deck-ids [deck-id]
               find-for [deck-id]]
          (let [child-deck-ids (->> find-for
                                    (map child-decks-for-deck)
                                    flatten
                                    (keep identity)
                                    (map :id))]
            (if (empty? child-deck-ids)
              deck-ids
              (recur (concat deck-ids child-deck-ids) ;; new deck-ids
                     child-deck-ids))))]              ;; new find-for

  (->> all-deck-ids
       (map #(->> @all-cards (where :deck-id %) vals))
       (keep identity)
       flatten
       )
  ))


(defn learned-cards [cards]
  (->> cards (filter #(-> % :reviews count (not= 0))) (sort-by :due)))


(defn learned-today [cards]
  (->> cards
       (filter #(and (-> % :reviews count (= 1))
                     (-> % :reviews last :date (= (cljs-time/today)))))))

(defn unlearned-cards [cards]
  (->> cards (filter #(-> % :reviews count (= 0))) (sort-by :sort)))

;; -- Persistence ----------------------------------------------------------
;;
;; For now the the entire application state is saved to LocalStorage after each
;; event. In the future we might selectively store only certain parts of the
;; state that gets stored. For instance, we may decide to not store the UI state.

(def local-storage-key "cljs-app-transit")

(def get-state-from-storage
  storage/get-from-file)

(defn set-state-to-storage [str]
  (storage/set-to-local-storage str)
  (storage/set-to-file str))

(defn store-state [str]
  (js/Promise. (fn [resolve reject]
                 (js/console.log "starting persistence")
    (storage/store-text "db.txt" str)
    (.setItem js/localStorage local-storage-key str))))

(defn state->local-storage []
  (-> (.resolve js/Promise)
      (.then #(storage/write-transit @state))
      ;(.then #(serialize @state))
      (.then set-state-to-storage)))

;(defonce myWorker (js/Worker. "js/bootstrap_worker.js"))

;(set! (.-onmessage myWorker) (fn [e]
  ;(js/console.log "Got back from worker, saving...")
  ;(.setItem js/localStorage local-storage-key (.-data e))))

;(defn state->local-storage []
  ;(js/console.log "Sending to worker")
  ;(js/console.log @state)
  ;(.postMessage myWorker @state))

(defn local-storage->state [state-from-storage]
  (reset! state (some->> state-from-storage
                         storage/read-transit)))

;  ;; Get state from file
;  ;; Get new actions from other file
;  ;; Re-play actions on state
;  ;; Persist new state to file
;  (let [state-from-file (read-edn "db.txt")
;        new-actions (read-edn "actions.txt")]
;    (local-storage->state)
;    (run! handle new-actions)
;    (state->local-storage)
;    store-text updated-state "db.txt")

(defn initialize-db []
  (let [state-from-storage (get-state-from-storage)]
    (if (nil? state-from-storage)
      (seed-data)
      (local-storage->state state-from-storage))))
