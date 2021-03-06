(ns app.models.card
  (:require [cljs-time.format :refer [formatter unparse]]
            [cljs-time.core   :as t]))

(defn tomorrow [] (-> (t/today) (t/plus (t/days 1))))

;; TODO: Test me
(defn formatted-due [card]
  (if (nil? (card :due))
    "Unlearned"
    (->> (card :due) (unparse (formatter "yyyy-MM-dd")))))

;; TODO: Test me
(defn progress [card]
  "Not Reviewed")

(def default-ease 2.0)

(defn at-least-tomorrow [dt]
  (if (t/after? (tomorrow) dt)
      (tomorrow)
      dt))

(defn remember [card]
  (let [last-review   (-> card :reviews last)
        last-due      (or (:due last-review) (t/today))
        last-interval (or (:interval last-review) 0.5)
        next-interval (* last-interval default-ease)
        next-due      (t/plus (t/today) (t/days (Math/round next-interval)))
        _ (js/console.log (str  "remember card " (name (:id card))))]

    (if (and (= (t/today) (:date last-review))
             (not (:learning? card))) ;;TODO: Test the not learning thing
      (do (js/console.log "already reviewed, not learning")
          card)

      (if (and (not (nil? last-review))
               (not (:remembered? last-review))
               (:learning? card))

        (do (js/console.log "is learning")
          (-> card
            (assoc :learning? false)))

        (do (js/console.log "is not learning (actual remember)")
         (-> card
             (assoc :due next-due)
             (assoc :learning? false)
             (update-in [:reviews]
               conj {:date (t/today)
                     :due next-due
                     :interval next-interval
                     :remembered? true}))
            )
        )
      )


  )
) 

;;TODO: Test this works with no reviews
(defn forget [card]
  (let [last-review   (-> card :reviews last)
        last-due      (or (:due last-review) (t/today))
        last-interval (or (:interval last-review) 0.5)
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
