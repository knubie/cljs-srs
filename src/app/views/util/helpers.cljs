(ns app.views.util.helpers)

(defn on-blur [fn]
  #(if-not (= (-> % .-target) (.-activeElement js/document))
     (fn %)))

(defn with-content [fn]
  #(-> % .-target .-textContent fn))
