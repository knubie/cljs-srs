(ns app.storage
  (:require [clojure.string :as str]
            [cljs-time.coerce :refer [to-local-date]]
            [cognitect.transit :as transit]))

(def local-storage-key "cljs-app-transit")

(def electron (js/require "electron"))
(def fs (js/require "fs"))
(def path (js/require "path"))
(defn mkdir [dir] (if-not (.existsSync fs dir) (.mkdirSync fs dir)))

(def user-data (-> electron .-remote .-app (.getPath "userData")))
(def user-data-media (.join path user-data "media"))

(defn store-media [file-path deck-id]
  (let [save-path (.join path user-data-media (name deck-id))
        file-name (.basename path file-path)
        save-file-path (.join path save-path file-name)]

    (mkdir user-data-media)
    (mkdir save-path)
    (->> file-path
         (.readFileSync fs)
         (.writeFileSync fs save-file-path))
    save-file-path))

(defn append-text [file-name text]
  (let [save-file-path (.join path user-data file-name)]
    (.appendFile fs save-file-path text #(js/console.log "Appended file."))))

(defn store-text [file-name text]
  (let [save-file-path (.join path user-data file-name)]
    (.writeFile fs save-file-path text #(js/console.log "Saved file."))))

(defn read-text [file-name]
  (let [read-file-path (.join path user-data file-name)]
    (.readFileSync fs read-file-path "utf8")))

(defn get-from-local-storage []
  (.getItem js/localStorage local-storage-key))

(defn set-to-local-storage [str]
  (js/Promise. (fn [resolve reject]
    (js/console.log "Starting set to LocalStorage.")
    (.setItem js/localStorage local-storage-key str))))

(defn get-from-file []
  (js/console.log "Starting get from file.")
  (read-text "db.txt"))

(defn set-to-file [str]
  (js/Promise. (fn [resolve reject]
    (js/console.log "Starting set to file.")
    (store-text "db.txt" str))))

(defn get-actions-from-file []
  (js/console.log "Starting get actions from file.")
  (read-text "actions.txt"))

(defn set-actions-to-file [str]
  (js/Promise. (fn [resolve reject]
    (js/console.log "Starting set to file.")
    (store-text "actions.txt" str))))

;; -- Transit --------------------------------------------------------------
;;
;; We need to set up some custom readers and writers to handle the date
;; objects that are stored in the database.

(def write-handlers
  {goog.date.Date (transit/write-handler
                    (constantly "t")
                    #(.getTime %)
                    #(-> % .getTime str))})

(def read-handlers
  {"t" (transit/read-handler
         #(-> % js/goog.date.UtcDateTime.fromTimestamp to-local-date))})

(def writer (transit/writer :json {:handlers write-handlers}))
(def reader (transit/reader :json {:handlers read-handlers}))

(defn write-transit [data]
  (transit/write writer data))

(defn read-transit [data]
  (transit/read reader data))

(defn read-edn [data]
  (edn/read-string {:readers {'inst to-local-date}}))

(defn write-edn [data]
  (prn-str data))

;(defn serialize [a]
  ;(js/Promise. (fn [resolve reject]
                 ;(js/console.log "starting serialization")
                 ;(resolve (prn-str a)))))
