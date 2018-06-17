(ns app.storage
  (:require [clojure.string :as str]))

(def electron (js/require "electron"))
(def fs (js/require "fs"))
(def path (js/require "path"))

(def user-data (-> electron .-remote .-app (.getPath "userData")))
(def user-data-media (.join path user-data "media"))

(defn store-media [file-path deck-id]
  (let [save-path (.join path user-data-media (name deck-id))
        file-name (.basename path file-path)]

    (.mkdirSync fs user-data-media)
    (.mkdirSync fs save-path)
    (->> file-path
         (.readFileSync fs)
         (.writeFileSync fs (.join path save-path file-name)))))
