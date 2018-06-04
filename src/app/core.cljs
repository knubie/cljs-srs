(ns app.core
  (:require [cljs.spec.alpha     :as spec]
            [reagent.core        :as r]
            [cljsjs.antd]
            [app.db              :refer [modal]]
            [app.events          :refer [dispatch]]
            [app.views.card      :refer [render-card3]]
            [app.views.side-bar  :refer [side-bar]]
            [app.views.workspace :refer [workspace]]
            [app.styles          :as styles]))
  

;; -- Markdown settings ----------------------------------------------------
;(def renderer (new (.Renderer js/marked)))

;(set! (.-heading renderer) (fn [text level]
  ;(str "<div style=\"color: rgb(34, 34, 34); font-weight: 700; letter-spacing: -0.003em; line-height: 1.1; width: 100%; margin-bottom: 0.25em; font-size: 2.375em; font-family: -apple-system, BlinkMacSystemFont, &quot;Segoe UI&quot;, Helvetica, &quot;Apple Color Emoji&quot;, Arial, sans-serif, &quot;Segoe UI Emoji&quot;, &quot;Segoe UI Symbol&quot;;\">" text "</div>")))

;(.setOptions js/marked (clj->js :renderer renderer))

;; -- View Components ------------------------------------------------------
;;
;; The main view component. Not much to see here.

(defn app []
  [:<>
   [:div {:style styles/app}
    [side-bar]
    [workspace]]

   [:> js/antd.Modal {:visible (boolean @modal) :footer nil
                      :onCancel #(dispatch [:reset-modal])}
    ;; TODO: Figure out how to keep this.
    (if (boolean @modal)[render-card3 @modal])]])

;; -- Entry Point ----------------------------------------------------------
;;
;; This function gets run in the index.html page.

(defn ^:export run []
  (r/render [app] (js/document.getElementById "app")))
  
