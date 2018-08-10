(ns app.worker)

;(def local-storage-key "cljs-app")

;(defn on-message [e]
  ;(->> e .-data js->clj prn-str))

(set! (.-onmessage js/self) (fn [e]
  (js/console.log "Worker got message")
  (js/console.log (.-data e))
  (let [meta (.-meta (.-data e))
        cnt (.-cnt (.-data e))
        arr (.-arr (.-data e))
        __hash (.-__hash (.-data e))
        foo (PersistentArrayMap. meta cnt arr __hash)]
    (js/console.log foo)
    (js/console.log (:db foo))
    )
  (as-> e stuff
       (.-data stuff)
       ;(js->clj stuff :keywordize-keys true)
       (prn-str stuff)
       (.postMessage js/self stuff))))
