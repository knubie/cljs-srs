(ns app.models.card-test
  (:require [cljs.spec.alpha  :as s]
            [cljs.test :refer-macros [deftest is testing run-tests]]
            [app.models.card  :refer [remember forget]]
            [clojure.spec.gen.alpha :as gen]
            [cljs-time.core   :refer [today yesterday plus minus days interval in-days]]
            [cljs-time.format :refer [formatter unparse]]))

;(s/def ::card   (s/keys :req-un [::id ::deck-id ::reviews ::fields]
                        ;:opt-un [::due]))

;(gen/generate (s/gen ::card))

;; If remembered? this-interval * interval-mod
;; else reduce interval-mod by .2 && this-interval / interval-mod

;; No reviews -> today + 1 -> set int-mod to 2
;; one review -> (due - last-review) * int-mod-of-last-review
;; two reviews -> (due - last-review) * int-mod-of-last-review
;; two reviews -> 

(defn tomorrow [] (-> (today) (plus (days 1))))

(def test-card
  {:id :foo
   :deck-id :bar
   :reviews []
   :fields []
   :due (today)})

;; -- Test Remember --------------------------------------------------------
;;
;; This function is called when a user remembers a card. In that case it
;; multiplies the pervious interval by the current ease factory
;; (defaults to 2.0).
;;
;; TODO: Refactor with `are` macro.

(deftest test-remember-with-no-reviews
  (let [card test-card]

    (is (= (-> card remember :due)
           (tomorrow))

          "Remembering a card with no review history will make it due tomorrow")

    (is (= (-> card remember :learning?)
           false)

          "Remembering a card removes it from the learning queue.")

    (is (= (-> card remember :reviews)
           [{:date        (today)
             :due         (tomorrow)
             :interval 1
             :remembered? true}])

          "Remembering a card with no review history will add a new review
           history item for today, and set the interval as 2.0")))


(deftest test-remember-with-one-review
  (let [card (assoc test-card :reviews
                    [{:date (today)
                      :due (tomorrow)
                      :interval 1
                      :remembered? true}])]
    
    (is (= (-> card remember :due)
           (plus (tomorrow) (days 2)))

          "Remembering a card with one review will make the due date equal
           to (due - last-review) * interval")
    
    (is (= (-> card remember :reviews last)
             {:date (today)
              :due (plus (tomorrow) (days 2))
              :interval 2
              :remembered? true})

          "Remembering a card will add a new review history item for today")))


(deftest test-remember-with-two-reviews
  (let [two-days-after-tomorrow (plus (tomorrow) (days 2)) 
        card (-> test-card
                 (assoc :due two-days-after-tomorrow)
                 (assoc :reviews
                    [{:date (today)
                      :due (tomorrow)
                      :interval 1
                      :remembered? true}

                     {:date (tomorrow)
                      :due (-> (today)
                               (plus (days 1))
                               (plus (days 2)))
                      :interval 2
                      :remembered? true}]))]
    
    (is (= (-> card remember :due)
             (-> (today)
                 (plus (days 1))
                 (plus (days 2))
                 (plus (days 4))))

          "Something about two reviews")
    
    (is (= (-> card remember :reviews last)
             {:date (today)
              :due (-> (today)
                       (plus (days 1))
                       (plus (days 2))
                       (plus (days 4)))
              :remembered? true
              :interval 4})

          "Remembering a card will add a new review history item for today")))


(deftest test-remember-with-two-reviews-and-one-failed
  (let [two-days-after-tomorrow (plus (tomorrow) (days 2)) 
        card (-> test-card
                 (assoc :due two-days-after-tomorrow)
                 (assoc :reviews
                    [{:date (today)
                      :due (tomorrow)
                      :interval 1
                      :remembered? true}

                     {:date (tomorrow)
                      :due (-> (today)
                               (plus (days 1))
                               (plus (days 1)))
                      :interval 1
                      :remembered? false}]))]
    
    (is (= (-> card remember :due)
             (-> (today)
                 (plus (days 1))
                 (plus (days 1))
                 (plus (days 2))))

          "Something about two reviews")
    
    (is (= (-> card remember :reviews last)
             {:date (today)
              :due (-> (today)
                       (plus (days 1))
                       (plus (days 1))
                       (plus (days 2)))
              :interval 2
              :remembered? true})

          "Remembering a card will add a new review history item for today")))


;; -- Test Forget ----------------------------------------------------------
;;
;; This method is called when a user couldn't remember a card. It sets the new
;; interval to the last good interval mutliplied by the new ease factor (which
;; is subtracted by 0.2 from the previous ease factor).


(deftest test-forget-with-one-review
  (let [card (assoc test-card :reviews
                    [{:date (yesterday)
                      :due (today)
                      :interval 1
                      :remembered? true}])]
    
    (is (= (-> card forget :due)
             (tomorrow))

          "Minimum interval is one day.")

    (is (= (-> card forget :learning?)
           true)

        "Marks the card as 'learning' to be reviewed again today.")
    
    (is (= (-> card forget :reviews last)
             {:date (today)
              :due (tomorrow)
              :interval 1
              :remembered? false})

          "Adds a review record to show that it was not remembered.")))

(deftest test-forget-with-one-review-past-due
  (let [card (assoc test-card :reviews
                    [{:date (minus (yesterday) (days 5))
                      :due (minus (today) (days 5))
                      :interval 1
                      :remembered? true}])]
    
    (is (= (-> card forget :due)
             (tomorrow))

          "TODO: Write me.")
    
    (is (= (-> card forget :reviews last)
             {:date (today)
              :due (tomorrow)
              :interval 1
              :remembered? false})

          "TODO: Write me")))

(deftest test-forget-twice-with-three-reviews
  (let [card (assoc test-card :reviews
                    [
                     {:date (yesterday)
                      :due (today)
                      :interval 1
                      :remembered? true}

                     {:date (today)
                      :due (-> (today)
                               (plus (days 2)))
                      :interval 2
                      :remembered? true}

                     {:date (today)
                      :due (-> (today)
                               (plus (days 2))
                               (plus (days 4)))
                      :interval 4
                      :remembered? true}

                     {:date (today)
                      :due (-> (today)
                               (plus (days 2))
                               (plus (days 4))
                               (plus (days 8)))
                      :interval 8
                      :remembered? true}
                     ])]
    
    (is (= (-> card forget forget :due)
             (-> (today)
                 (plus (days 2))
                 (plus (days 4))
                 (plus (days 8))
                 (plus (days 4))
                 (plus (days 2))))

          "TODO: Write me.")
    
    (is (= (-> card forget forget :reviews last)
             {:date (today)
              :due (-> (today)
                       (plus (days 2))
                       (plus (days 4))
                       (plus (days 8))
                       (plus (days 4))
                       (plus (days 2)))
              :interval 2
              :remembered? false})

          "TODO: Write me")))
