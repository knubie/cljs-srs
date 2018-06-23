(ns app.views.data-table
  (:require [reagent.core   :as r]
            [app.events     :refer [dispatch]]
            [app.models.card :as c]
            [app.views.icons :as icons]
            [app.views.util.keyboard :as kbd]
            [app.styles     :as styles]))

(def border-strong "1px solid rgb(221, 225, 227)")
(def border-weak "1px solid rgb(243, 243, 243)")
(def weak-color "rgb(153, 153, 153)")
(def meta-data-count 2)

(defn table-columns [fields meta-data deck-id]
  [:div {:style {:display       'flex
                 :border-top    border-strong
                 :border-bottom border-strong
                 :color weak-color}}
 
   ;; Fields
   
    (for [field fields]
      [:div {:key (field :id)
             :content-editable true
             :suppress-content-editable-warning true
             :on-blur #(dispatch [:edit-field
                    (assoc field :name (-> % .-target .-textContent))
                                  ])
             :style {:display      'flex
                     :align-items  'center
                     :-webkit-user-modify 'read-write-plaintext-only
                     :outline      0
                     :padding      "0 8px"
                     :height       32
                     :width        (/ (- 900 32) (+ meta-data-count (count fields)))
                     :border-right border-weak}}
       (field :name)])

    (for [datum meta-data]
      [:div {:key (datum :name)
             :style {:display      'flex
                     :align-items  'center
                     :padding      "0 8px"
                     :height       32
                     :width        (/ (- 900 32) (+ meta-data-count (count fields)))
                     :border-right border-weak}}
       (datum :name)])

 
   ;; Add Field
   [:div {:on-click #(dispatch [:add-field {:deck-id deck-id
                                            :name "New Field"
                                            :type "image"}])
          :style {:display 'flex
                  :align-items 'center
                  :justify-content 'center
                  :flex-grow 1
                  :color weak-color
                  :width 32}}
    [icons/plus 18 18]]])

  ;(r/with-let [hover? (r/atom false)]
    ;[:div {:on-mouse-enter #(reset! hover? true)
           ;:on-mouse-leave #(reset! hover? false)}

(defn text-edit [{:keys [text on-save]}]
  (r/with-let [save #(let [v (-> % str clojure.string/trim)]
                       (if-not (empty? v) (on-save v)))]

    [:div {:content-editable true
           :on-blur #(-> % .-target .-textContent save)
           :on-key-down #(case (.-which %)
                           kbd/enter  (-> % .-target .blur)
                           kbd/escape (-> % .-target .blur)
                           nil)
           :style {:-webkit-user-modify 'read-write-plaintext-only
                   :outline 0}}
     text]))

(def table-cell-text-edit
  (with-meta text-edit
    {:component-did-mount (fn [el]
      ;; All this bullshit just to get the curdor at the
      ;; end of the text.
      (let [range (js/document.createRange)
            sel (js/window.getSelection)]
        (.focus (r/dom-node el))
        (.selectNodeContents range (r/dom-node el))
        (.collapse range false)
        (.removeAllRanges sel)
        (.addRange sel range)))}))



(def table-cell-style
  {:display     'flex
   :align-items 'center
   :padding      "5px 8px 6px"
   :border-right border-weak
   :cursor       'pointer})

(defn table-cell [field record width]
  (r/with-let [editing? (r/atom false)]

    [:div {:key (field :id)
           :on-click #(reset! editing? true)
           :style (merge table-cell-style {:width width})}

     (if @editing?
       [table-cell-text-edit {:text (-> record :fields ((field :id)))
                              :on-save #(do
                                (dispatch [:edit-card-field {
                                  :card-id     (record :id)
                                  :field-id    (field :id)
                                  :field-value %}])
                                (reset! editing? false))}]

       (case (field :type)
         "text" (-> record :fields ((field :id)))
         "image" [:img {:src (-> record :fields ((field :id)))}]
         "audio" (-> record :fields ((field :id))))
    )]))

(defn table-row [meta-data fields record]
  (r/with-let [hover? (r/atom false)]

    [:div {:key (record :id)
           :on-mouse-enter #(reset! hover? true)
           :on-mouse-leave #(reset! hover? false)
           :style {:display       'flex
                   :position      'relative
                   :border-bottom border-strong}}

     (if @hover?
       [:div {:style styles/pop-out-button
              :on-click #(dispatch [:set-modal (record :id)])}
        [icons/popup 10 10] "Preview"])

     (for [field fields] ^{:key (:id field)}
       [table-cell field record (/ (- 900 32) (+ meta-data-count (count fields)))])
       ;; Field
       ;[:div {:key (field :id)
              ;:content-editable true
              ;:on-blur #(dispatch [:edit-card-field {
                ;:card-id     (record :id)
                ;:field-id    (field :id)
                ;:field-value (-> % .-target .-textContent)}])
              ;:style {:display     'flex
                      ;:align-items 'center
                      ;:-webkit-user-modify 'read-write-plaintext-only
                      ;:outline      0
                      ;:padding      "5px 8px 6px"
                      ;:border-right border-weak
                      ;:width        (/ (- 900 32) (+ meta-data-count (count fields)))}}
        ;(-> record :fields ((field :id)))])


    (for [datum meta-data]
      [:div {:key (datum :name)
             :style {:display      'flex
                     :align-items  'center
                     :cursor 'default
                     :padding      "0 8px"
                     :height       32
                     :width        (/ (- 900 32) (+ meta-data-count (count fields)))
                     :border-right border-weak}}
       (-> record ((datum :fn)))])


     ;; Add-Field Column
     [:div {:style {:width 32 :flex-grow 1}}]]))


(defn table-rows [fields meta-data records]
  [:div
   (for [record records]
     ^{:key (record :id)} [table-row meta-data fields record])])


(defn table-new-record [deck-id fields]
  [:div {:on-click #(dispatch [:add-empty-card deck-id fields])
         :style {:display 'flex
                 :align-items 'center
                 :color weak-color
                 :border-bottom border-weak
                 :height 32
                 :padding-left 8
                 :padding-bottom 2
                 :cursor 'pointer}}
   [icons/plus 18 18 4] "Add a Card"])


(defn data-table [fields records deck-id]
  (let [meta-data [{:name "Due"      :fn c/formatted-due}
                   {:name "Progress" :fn c/progress}]]
    [:div
     [table-columns fields meta-data deck-id]
     [table-rows    fields meta-data records]
     [table-new-record deck-id fields]]))
