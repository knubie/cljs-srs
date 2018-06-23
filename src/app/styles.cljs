(ns app.styles)

(def app {
  :display        "flex"
  :flex-direction "row"
  :height         "100vh"
})

(def side-bar {
  :padding-top      "45px"
  :background-color "#F9F7F4"
  :color            "#787877"
  :text-align       "left"
  :width            "240px"
  :flex-direction   'column
})

(def side-bar-header {
  :letter-spacing "0.03em"
  :text-transform 'uppercase
  :font-size 11.5
  :line-height 1
  :margin-bottom 1
  :color "rgb(197, 196, 195)"})

(def workspace {
  :background-color "#FFFFFF"
  :color "#222222"
  :flexGrow 1
  :text-align "center"
  :overflow 'auto
})

(def workspace-content {
  :width "900px"
  :margin "0 auto"
  :margin-top "5em"
  :text-align "left"
  :padding "0px 96px 30vh"
})

(def table-td {
  :border "1px solid #DEE1E3"
})

(def text-input {
  :border 'none
  :outline 'none
  :height "21px"
})

(def border-strong "1px solid rgb(221, 225, 227)")
(def border-weak "1px solid rgb(243, 243, 243)")
(def weak-color "rgb(153, 153, 153)")

(def table-columns {
  :display       'flex
  :border-top    border-strong
  :border-bottom border-strong
  :color         weak-color})

(defn table-field-column [field-count] {
  :display      'flex
  :align-items  'center
  ;:-webkit-user-modify 'read-write-plaintext-only
  ;:outline      0
  :padding      "0 8px"
  ;:flex-shrink  0
  :min-height   32
  :border-right border-weak
  :width        (/ (- 900 32) field-count)})

(def table-cell {
  :display     'flex
  :align-items 'center
  :padding      "5px 8px 6px"
  :border-right border-weak
  :cursor       'pointer})

(def add-field {
  :display 'flex
  :flex-grow 1
  :width 32})

(def pop-out-button {
    :position 'absolute
    :top 4 :right 6

    :align-items     'center
    :user-select     'none
    :display         'flex
    :justify-content 'center
    :height          24
    :padding         "2px 6px"
    :background      'white
    :border          'none
    :flex-shrink     0
    :border-radius   3
    :color "rgb(165, 165, 165)"
    :box-shadow "rgba(0, 0, 0, 0.08) 0px 0px 1px, rgba(84, 70, 35, 0.14) 0px 1px 3px, rgba(84, 70, 35, 0.2) 0px 1px 8px"
    :font-size 12
    :text-transform 'uppercase})

;; -- Typography -----------------------------------------------------------

(def h1 {
  :color "rgb(34, 34, 34)"
  :font-weight 700
  :letter-spacing "-0.003em"
  :line-height "1.1"
  :width "100%"
  :margin-bottom "0.25em"
  :font-size "2.375em"
  :font-family "-apple-system, BlinkMacSystemFont, \"Segoe UI\", Helvetica, \"Apple Color Emoji\", Arial, sans-serif, \"Segoe UI Emoji\", \"Segoe UI Symbol\""})
