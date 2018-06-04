(ns app.models.card
  (:require [cljs-time.format :refer [formatter unparse]]
            [cljs-time.core   :refer [today plus days]]))

(defn formatted-due [card]
  (if (nil? (card :due))
    "Unlearned"
    (->> (card :due) (unparse (formatter "yyyy-MM-dd")))))

(defn remember [card]
  (-> card
    (assoc :due (-> (today) (plus (days 2))))
    (update-in [:reviews]
      (conj {:date (today)
            ;; TODO: add easing?
            :remembered? true}))))
