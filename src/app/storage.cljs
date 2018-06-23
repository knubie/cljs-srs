(ns app.storage
  (:require [clojure.string :as str]))

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
