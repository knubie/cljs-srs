(ns app.views.side-bar.section
  (:require [reagent.core    :as r]
            [app.styles      :as styles]))

(defn section [{:keys [title]} & children]
  [:div {:style {:margin-bottom 20}}
   [:div {:style {:display 'flex
                  :align-items 'center
                  :min-height 24
                  :font-size 14
                  :padding "0px 14px 0px 15px"
                  :width "100%"}}
    [:span {:style styles/side-bar-header} title]]
   children])
