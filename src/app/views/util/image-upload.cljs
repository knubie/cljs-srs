(ns app.views.util.image-upload
  (:require [reagent.core   :as r]
            [app.storage     :as storage]))

(defn image-upload [{:keys [dir on-upload]}]
   [:input {:name "image-upload"
            :type "file"
            :on-change (fn [e]
              (let [file (-> e .-target .-files (.item 0) .-path )
                    uploaded-path (storage/store-media file dir)]
                (on-upload uploaded-path)))}])
