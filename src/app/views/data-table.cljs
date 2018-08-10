(ns app.views.data-table
  (:require [reagent.core   :as r]
            [app.db         :as db]
            [cljsjs.antd]
            [cljsjs.react-virtualized]
            [app.events     :refer [dispatch]]
            [app.models.card :as c]
            [app.views.util.image-upload :refer [image-upload]]
            [app.views.icons :as icons]
            [app.views.util.keyboard :as kbd]
            [app.styles     :as styles]))

(def border-strong "1px solid rgb(221, 225, 227)")
(def border-weak "1px solid rgb(243, 243, 243)")
(def weak-color "rgb(153, 153, 153)")
(def meta-data-count 3)
(def content-width 900)

(defn table-columns [fields meta-data deck-id]
  [:div {:style styles/table-columns}
 
   ;; Fields
   
    (for [field fields]
      ;;[icons/attach 13 13 4] 
      [:div {:key (field :id)
             :content-editable true
             :suppress-content-editable-warning true
             :on-blur #(dispatch [:db/edit-field
               (assoc field :name (-> % .-target .-textContent))])
             :style (styles/table-field-column (+ meta-data-count (count fields)))}

       (field :name)])

    [:div {:key "notes"
           :style (styles/table-field-column (+ meta-data-count (count fields)))}
      "Notes"]

   ;; Meta Data

    (for [datum meta-data]
      [:div {:key (datum :name)
             :style (styles/table-field-column (+ meta-data-count (count fields)))}

       (datum :name)])

 
   ;; Add Field

   [:div {:on-click #(dispatch [:db/add-field {:deck-id deck-id
                                               :name "Untitled"
                                               :type "text"}])
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

(defn text-edit [{:keys [text width on-save]}]
  (r/with-let [save #(-> % str clojure.string/trim on-save)]

    [:div {:content-editable true
           :style (styles/editing-table-cell width)
           :on-blur #(-> % .-target .-textContent save)
           :on-key-down #(case (.-which %)
                           ;kbd/enter  (-> % .-target .blur)
                           kbd/escape (-> % .-target .blur)
                           nil)}
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



(defmulti table-cell (fn [field & _] (:type field)))

(defmethod table-cell "text" [field record width]
  (r/with-let [editing? (r/atom false)]

    (if @editing?
      ;; The width + 1 is for the 1px border
      [:div {:style {:width (+ width 4) :position "relative"}}
       [table-cell-text-edit {:text (-> record :fields ((field :id)))
                              :width width
                              :on-save #(do
                                (dispatch [:db/edit-card-field {
                                  :card-id     (record :id)
                                  :field-id    (field :id)
                                  :field-value %}])
                                (reset! editing? false))}]]
      [:div {:key (field :id)
             :on-click #(reset! editing? true)
             :style (merge styles/table-cell {:width width})}
        (-> record :fields ((field :id)))])))

(defmethod table-cell "image" [field record width]
  [:> js/antd.Popover {:placement "bottom"
                       :title "Upload"
                       :trigger "click"
                       :content (r/as-element
                         [image-upload
                           {:dir (record :deck-id)
                            :on-upload #(dispatch
                              [:db/edit-card-field {
                                :card-id     (record :id)
                                :field-id    (field :id)
                                :field-value (js/encodeURI %)}])}])}

    [:div {:key (field :id)
           :style (merge styles/table-cell {:width width})}

       [:img {:src (-> record :fields ((field :id)))}]]])


(defmethod table-cell "audio" [field record width]
  [:> js/antd.Popover {:placement "bottom"
                       :title "Upload"
                       :trigger "click"
                       :content (r/as-element
                         [image-upload
                           {:dir (record :deck-id)
                            :on-upload #(dispatch
                              [:db/edit-card-field {
                                :card-id     (record :id)
                                :field-id    (field :id)
                                :field-value (js/encodeURI %)}])}])}

    [:div {:key (field :id)
           :style (merge styles/table-cell {:width width})}

       [:audio {:src (-> record :fields ((field :id)))
                :style {:width 70}
                :controls true}]]])


(defmethod table-cell "notes" [field record width]
  [:div {:key (field :id)
         :style (merge styles/table-cell {:width width})}

   [:> js/antd.Select {:mode "multiple"
                       :style {:width "100%"}
                       :placeholder "Add some notes"
                       :onChange #(js/console.log %)}

    (for [[id note] @db/all-notes] ^{:key (note :id)}
      [:> js/antd.Select.Option (note :name)])]])


(defn table-row [meta-data fields record]
  (r/with-let [hover? (r/atom false)
               ]

    (let [column-width (/ (- content-width 32) (+ meta-data-count (count fields)))
          ]

      [:div {
             :on-mouse-enter #(reset! hover? true)
             :on-mouse-leave #(reset! hover? false)
             :style 
                      {:display       'flex
                       :position      'relative
                       :border-bottom border-strong}
                      
             }

       (if @hover?
         [:div {:style styles/pop-out-button
                :on-click #(dispatch [:ui/set-modal (record :id)])}
          [icons/popup 10 10] "Preview"])

       (for [field fields] ^{:key (:id field)}
         [table-cell field record column-width])

      [table-cell {:type "notes"} nil column-width]

      (for [datum meta-data]
        [:div {:key (datum :name)
               :style {:display      'flex
                       :align-items  'center
                       :cursor       'default
                       :padding      "0 8px"
                       :min-height   32
                       :width        column-width
                       :border-right border-weak}}
         (-> record ((datum :fn)))])


       ;; Add-Field Column
       [:div {:style {:width 32 :flex-grow 1}}]])))


;(defn table-rows [fields meta-data records]
  ;[:div
   ;(for [record records]
     ;^{:key (record :id)} [table-row meta-data fields record])])


; 49 -real
; 58 - expanded
(defn table-rows [fields meta-data records]
  (let [foobar (fn [opts]

                    (r/as-element 
                      [:div {:key (.-key opts) :style (.-style opts)}
                      [table-row meta-data fields
                                   (nth records (.-index opts))
                                   
                                   ]
                       ]
                      )
                 )]
    [:> js/ReactVirtualized.List {:width       710
                                  :height      700
                                  :rowHeight   58
                                  :rowCount    (count records)
                                  :rowRenderer foobar}]))

(defn table-new-record [deck-id fields]
  [:div {:on-click #(dispatch [:db/add-empty-card deck-id fields])
         :style styles/table-new-record}
   [icons/plus 18 18 4] "Add a Card"])


(defn data-table [fields records deck-id]
  (let [meta-data [{:name "Due"      :fn c/formatted-due}
                   {:name "Progress" :fn :sort}]]
    [:div
     [table-columns fields meta-data deck-id]
     [table-rows    fields meta-data records]
     [table-new-record deck-id fields]]))
