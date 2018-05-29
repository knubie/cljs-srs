(ns app.core
  (:require [cljs.spec.alpha     :as spec]
            [reagent.core        :as r]
            [cljsjs.antd]
            [cljs-time.core      :as cljs-time]
            [app.db              :refer [state ui-workspace]]
            [app.views.side-bar  :refer [side-bar]]
            [app.views.workspace :refer [workspace]]
            [app.styles          :as styles]))
  
;; -- View Components ------------------------------------------------------

;(defn ant-app []
  ;[:> js/antd.Layout
   ;[:> js/antd.Layout.Sider {:width 240 :style {:background "#F9F7F4"}}
    ;[side-bar]]
    ;;[side-bar/side-bar {:decks (-> @db-decks vals)}]]
    ;;[:p "I'm a little sidebar short and stout!"]]
   ;[:> js/antd.Layout.Content [workspace @ui-workspace]]]
  ;)

(def all-decks (r/cursor state [:db :decks]))

(defn app []
  [:div {:style styles/app}
   
    [side-bar {:decks (-> @all-decks vals)}]
    [workspace @ui-workspace]])

;; -- Entry Point ----------------------------------------------------------
;;
;; This function gets run in the index.html page.

(defn ^:export run []
  (r/render [app] (js/document.getElementById "app")))
  
