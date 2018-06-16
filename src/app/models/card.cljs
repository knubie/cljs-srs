(ns app.models.card
  (:require [cljs-time.format :refer [formatter unparse]]
            [cljs-time.coerce :refer [to-local-date]]
            [cljs-time.core   :as t]))

;; TODO: Test me
(defn formatted-due [card]
  (if (nil? (card :due))
    "Unlearned"
    (->> (card :due) to-local-date (unparse (formatter "yyyy-MM-dd")))))

;; TODO: Test me
(defn progress [card]
  "Newbie")

(def default-ease 2.0)

(defn tomorrow [] (-> (t/today) (t/plus (t/days 1))))

(defn at-least-tomorrow [dt]
  (if (t/after? (tomorrow) dt)
      (tomorrow)
      dt))

(defn remember [card]
  (let [last-review   (-> card :reviews last)
        last-due      (or (:due last-review) (t/today))
        last-interval (or (:interval last-review) 0.5)
        next-interval (* last-interval default-ease)
        next-due      (t/plus last-due (t/days (Math/round next-interval)))]

   (-> card
    (assoc :due next-due)
    (assoc :learning? false)
    (update-in [:reviews]
      conj {:date (t/today)
            :due next-due
            :interval next-interval
            :remembered? true})))) 

(defn forget [card]
  (let [last-review   (-> card :reviews last)
        last-due      (or (last-review :due) (t/today))
        last-interval (or (last-review :interval) 0.5)
        next-interval (-> last-interval (/ default-ease) (max 1))
        next-due      (->> next-interval Math/round t/days (t/plus last-due) at-least-tomorrow)]

    (if (nil? last-review)
        card 
        (-> card
          (assoc :due next-due)
          (assoc :learning? true)
          (update-in [:reviews]
            conj {:date (t/today)
                  :due next-due
                  :interval next-interval
                  :remembered? false})))))
