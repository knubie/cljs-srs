(ns app.views.icons
  (:require [reagent.core    :as r]
            [app.styles      :as styles]))

(defn book [w h]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :fill 'currentcolor}
         :view-box "0 0 700 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M682 256c12 5.333 18 14.667 18 28c0 0 0 562 0 562c0 9.333 -4 17.667 -12 25c-8 7.333 -17.333 11 -28 11c-30.667 0 -46 -12 -46 -36c0 0 0 -522 0 -522c0 -8 -4 -14 -12 -18c0 0 -404 -216 -404 -216c-21.333 -6.667 -44 -3.333 -68 10c-29.333 13.333 -48 28 -56 44c0 0 408 228 408 228c12 5.333 18 14.667 18 28c0 0 0 550 0 550c0 14.667 -6 24 -18 28c-4 2.667 -9.333 4 -16 4c-9.333 0 -16 -1.333 -20 -4c-5.333 -4 -72.667 -46.333 -202 -127c-129.333 -80.667 -200 -124.333 -212 -131c-17.333 -12 -26 -23.333 -26 -34c0 0 -6 -524 -6 -524c0 -18.667 4.667 -36 14 -52c18.667 -30.667 52.667 -56.333 102 -77c49.333 -20.667 88 -23.667 116 -9c0 0 450 232 450 232"}]]])

(defn doc-text [w h]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :fill 'currentcolor}
         :view-box "0 0 700 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M212 542c0 0 0 -90 0 -90c0 0 280 0 280 0c0 0 0 90 0 90c0 0 -280 0 -280 0c0 0 0 0 0 0m388 -492c28 0 51.667 9.667 71 29c19.333 19.333 29 43 29 71c0 0 0 700 0 700c0 26.667 -9.667 50 -29 70c-19.333 20 -43 30 -71 30c0 0 -500 0 -500 0c-26.667 0 -50 -10 -70 -30c-20 -20 -30 -43.333 -30 -70c0 0 0 -700 0 -700c0 -28 10 -51.667 30 -71c20 -19.333 43.333 -29 70 -29c0 0 500 0 500 0c0 0 0 0 0 0m0 800c0 0 0 -700 0 -700c0 0 -500 0 -500 0c0 0 0 700 0 700c0 0 500 0 500 0c0 0 0 0 0 0m-110 -592c0 0 0 88 0 88c0 0 -280 0 -280 0c0 0 0 -88 0 -88c0 0 280 0 280 0c0 0 0 0 0 0m0 392c0 0 0 88 0 88c0 0 -280 0 -280 0c0 0 0 -88 0 -88c0 0 280 0 280 0c0 0 0 0 0 0"}]]])

(defn popup [w h]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :margin-right 4 ;; TODO: Generalize this.
                 :fill 'currentcolor}
         :view-box "0 0 700 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M700 100c28 0 51.667 9.667 71 29c19.333 19.333 29 43 29 71c0 0 0 400 0 400c0 26.667 -9.667 50 -29 70c-19.333 20 -43 30 -71 30c0 0 -400 0 -400 0c-26.667 0 -50 -10 -70 -30c-20 -20 -30 -43.333 -30 -70c0 0 0 -402 0 -402c0 -26.667 9.667 -49.667 29 -69c19.333 -19.333 43 -29 71 -29c0 0 400 0 400 0c0 0 0 0 0 0m0 500c0 0 0 -400 0 -400c0 0 -400 0 -400 0c0 0 0 400 0 400c0 0 400 0 400 0c0 0 0 0 0 0m-600 -100c0 0 0 300 0 300c0 0 300 0 300 0c0 0 0 100 0 100c0 0 -300 0 -300 0c-26.667 0 -50 -10 -70 -30c-20 -20 -30 -43.333 -30 -70c0 0 0 -300 0 -300c0 0 100 0 100 0c0 0 0 0 0 0"}]]])

(defn plus [w h mr]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :margin-right mr
                 :fill 'currentcolor}
         :view-box "0 0 700 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M550 450c20 0 30 16.667 30 50c0 33.333 -10 50 -30 50c0 0 -210 0 -210 0c0 0 0 210 0 210c0 20 -16.667 30 -50 30c-33.333 0 -50 -10 -50 -30c0 0 0 -210 0 -210c0 0 -210 0 -210 0c-20 0 -30 -16.667 -30 -50c0 -33.333 10 -50 30 -50c0 0 210 0 210 0c0 0 0 -210 0 -210c0 -20 16.667 -30 50 -30c33.333 0 50 10 50 30c0 0 0 210 0 210c0 0 210 0 210 0c0 0 0 0 0 0"}]]])

(defn language [w h mr]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :margin-right mr
                 :fill 'currentcolor}
         :view-box "0 0 1000 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M988 544c20 54.667 16.667 113.333 -10 176c-26.667 62.667 -71.333 116 -134 160c-6.667 0 -10.667 -0.667 -12 -2c-1.333 -1.333 -6.667 -7.667 -16 -19c-9.333 -11.333 -14.667 -17.667 -16 -19c-1.333 -4 -0.667 -7.333 2 -10c57.333 -40 96.333 -90.667 117 -152c20.667 -61.333 17 -110.667 -11 -148c-10.667 25.333 -23.667 50.667 -39 76c-15.333 25.333 -35 52 -59 80c-24 28 -52.667 49.667 -86 65c-33.333 15.333 -68.667 20.333 -106 15c-34.667 -4 -62.667 -17.667 -84 -41c-21.333 -23.333 -32 -54.333 -32 -93c0 -56 20 -105.333 60 -148c33.333 -33.333 71.333 -55.333 114 -66c0 0 -2 -100 -2 -100c-93.333 16 -142 24 -146 24c-4 1.333 -7.333 0 -10 -4c0 -1.333 -1.667 -11 -5 -29c-3.333 -18 -5 -28.333 -5 -31c-1.333 -1.333 -1 -2.667 1 -4c2 -1.333 4.333 -2 7 -2c0 0 156 -28 156 -28c0 -73.333 -0.667 -111.333 -2 -114c0 -5.333 2.667 -8 8 -8c30.667 0 48 -0.667 52 -2c6.667 0 10 2.667 10 8c0 0 0 104 0 104c105.333 -14.667 160 -22 164 -22c5.333 -2.667 8.667 -0.667 10 6c0 1.333 1.333 9 4 23c2.667 14 4 22.333 4 25c2.667 6.667 1.333 10.667 -4 12c0 0 -176 30 -176 30c0 0 0 102 0 102c0 0 12 0 12 0c57.333 0 106.667 12 148 36c41.333 24 70 57.333 86 100c0 0 0 0 0 0m-370 160c18.667 4 39.333 2 62 -6c0 0 -4 -214 -4 -214c-22.667 8 -42.667 21.333 -60 40c-29.333 29.333 -44 65.333 -44 108c0 44 15.333 68 46 72c0 0 0 0 0 0m122 -28c18.667 -16 38 -38.667 58 -68c20 -29.333 35 -55.667 45 -79c10 -23.333 12.333 -37 7 -41c-24 -12 -56 -18 -96 -18c-1.333 0 -3.333 0.333 -6 1c-2.667 0.667 -4.667 1 -6 1c0 0 -2 204 -2 204m-448 -382c6.667 18.667 24.333 73.667 53 165c28.667 91.333 56.333 178.333 83 261c26.667 82.667 40 124.667 40 126c0 2.667 -1.333 4 -4 4c0 0 -86 0 -86 0c-4 0 -6 -1.333 -6 -4c0 0 -50 -166 -50 -166c0 0 -176 0 -176 0c-32 109.333 -48.667 164.667 -50 166c0 2.667 -2 4 -6 4c0 0 -86 0 -86 0c-2.667 0 -4 -1.333 -4 -4c6.667 -12 65.333 -196 176 -552c1.333 -5.333 4.667 -8 10 -8c0 0 96 0 96 0c6.667 0 10 2.667 10 8c0 0 0 0 0 0m-130 316c0 0 144 0 144 0c0 0 -72 -264 -72 -264c0 0 -72 264 -72 264"}]]])

(defn pencil [w h mr]
  [:svg {:style {:display 'flex
                 :width w
                 :height h
                 :margin-right mr
                 :fill 'currentcolor}
         :view-box "0 0 780 1000" :xmlns "http://www.w3.org/2000/svg"}
   [:g
     [:path {:d "M718 170c21.333 21.333 37 42.667 47 64c10 21.333 15 37.333 15 48c0 0 0 16 0 16c0 0 -252 252 -252 252c0 0 -290 288 -290 288c0 0 -238 52 -238 52c0 0 50 -240 50 -240c0 0 290 -288 290 -288c0 0 252 -252 252 -252c36 -8 78 12 126 60c0 0 0 0 0 0m-494 640c0 0 24 -24 24 -24c-1.333 -29.333 -18.667 -60.667 -52 -94c-14.667 -14.667 -29.667 -26.333 -45 -35c-15.333 -8.667 -27 -13 -35 -13c0 0 -14 -2 -14 -2c0 0 -22 24 -22 24c0 0 -18 80 -18 80c18.667 10.667 34 22 46 34c16 16 28 32 36 48c0 0 80 -18 80 -18"}]]])