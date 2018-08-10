(ns app.core
  (:require [reagent.core        :as r]
            [cljsjs.antd]
            [app.dnd             :as dnd]
            [app.db              :refer [state modal initialize-db]]
            [app.events          :refer [dispatch]]
            [app.views.card      :refer [render-card3]]
            [app.views.side-bar  :refer [side-bar]]
            [app.views.workspace :refer [workspace]]
            [app.views.ui        :as ui]
            [app.styles          :as styles]))


;; -- Markdown settings ----------------------------------------------------
;(def renderer (new (.-Renderer js/marked)))

;(set! (.-heading renderer) (fn [text level] "SUCK"))
;;(set! (.-heading renderer) (fn [text level]
  ;;(str "<div style=\"color: rgb(34, 34, 34); font-weight: 700; letter-spacing: -0.003em; line-height: 1.1; width: 100%; margin-bottom: 0.25em; font-size: 2.375em; font-family: -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, Helvetica, &quot;Apple Color Emoji&quot;, Arial, sans-serif, &quot;Segoe UI Emoji&quot;, &quot;Segoe UI Symbol&quot;;\">" text "</div>")))

;(.setOptions js/marked (clj->js :renderer renderer))

;(js/console.log (js/marked "# foo" {:renderer renderer}))

;(def electron       (js/require "electron"))
;(-> electron .-remote .-app (.getPath "userData") js/console.log)

;fs.readFileSync('<directory>')
;fs.writeFileSync(file)

;; TODO: Track readiness via state atom.
(.init js/kuroshiro #js{:dicPath "dict"})


;; -- React DnD  -----------------------------------------------------------

(def react->reagent r/adapt-react-class)
(def reagent->react r/reactify-component)

;; -- Data Initialization --------------------------------------------------
;;
;; This is where we initialize the data. First, we look in localStorage
;; and parse the edn if there is any. If not, we seed some data.
;;
;; See app.db/initialize-db for more info.

(initialize-db)

;; -- Worker shit ----------------------------------------------------------
;var myWorker = new Worker('worker.js');
;(def myWorker (js/Worker. "js/worker.js"))
;myWorker.postMessage([first.value,second.value]);
;(js/console.log "start message")
;(.postMessage myWorker (clj->js @state))
;(js/console.log "end message")
;myWorker.onmessage = function(e) {
  ;console.log('Message received from worker');
;}


;; -- View Components ------------------------------------------------------
;;
;; The main view component. Not much to see here.

(defn app []
  [:<>
   [:div {:style styles/app}
    [side-bar]
    [workspace]]

   [:> js/antd.Modal {:visible (@modal :open?)
                      :bodyStyle {:padding "72px"}
                      :width nil
                      :footer nil
                      :closable false
                      :onCancel #(dispatch [:ui/close-modal])}
    (if (boolean (@modal :card-id))
      [:div
       [:div {:style {:height 45
                      :display 'flex
                      :justify-content 'space-between
                      :align-items 'center
                      :padding-left 12
                      :padding-righ 12}}]
       [render-card3 (@modal :card-id)]]
    )]])

(defn with-drag-drop-context [app]
  (-> app
      reagent->react
      ((js/ReactDnD.DragDropContext (.-default js/ReactDnDHTML5Backend)))
      react->reagent))

;; -- Entry Point ----------------------------------------------------------
;;
;; This function gets run in the index.html page.

(defn ^:export run []
  (r/render
    [(dnd/with-drag-drop-context dnd/html-backend app)]
    (js/document.getElementById "app")))
