(ns app.views.ui
  (:require [reagent.core   :as r]))

(defn button [content on-click]
  (r/with-let [style (r/atom {:align-items 'center
                       :user-select 'none
                       :display 'inline-flex
                       :padding "4px 8px"
                       :border-radius "2px"
                       :font-size "14px"
                       :cursor 'pointer})]

    [:div {:on-click on-click
           :on-mouse-enter #(swap! style assoc :background "rgba(58, 56, 52, 0.08)")
           :on-mouse-leave #(swap! style dissoc :background)
           :style @style}
     content]))
