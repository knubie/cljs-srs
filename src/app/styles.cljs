(ns app.styles)

(def sidebar-background       "#F9F7F4")
(def sidebar-foreground       "#787877")
(def sidebar-header           "rgb(197, 196, 195)")
(def workspace-background     "#FFFFFF")
(def workspace-foreground     "#222222")
(def table-border             "#DEE1E3")
(def border-strong            "1px solid rgb(221, 225, 227)")
(def border-weak              "1px solid rgb(243, 243, 243)")
(def weak-color               "rgb(153, 153, 153)")
(def popout-button-foreground "rgb(165, 165, 165)")
(def h1-foreground            "rgb(34, 34, 34)")


(def app {
  :display        "flex"
  :flex-direction "row"
  :height         "100vh"
})

(def side-bar {
  :padding-top      "45px"
  :background-color sidebar-background
  :color            sidebar-foreground
  :text-align       "left"
  :width            "240px"
  :flex-direction   'column
})

(def side-bar-header {
  :letter-spacing "0.03em"
  :text-transform 'uppercase
  :font-size      11.5
  :line-height    1
  :margin-bottom  1
  :color          sidebar-header
})

(def side-bar-count {
  :margin-left   4
  :background    "rgba(0,0,0,0.1)"
  :color         "white"
  :border-radius 2
  :font-size     10
  :padding       "1px 3px 2px 3px"
  :font-weight   700
})

(def workspace {
  :background-color workspace-background
  :color            workspace-foreground
  :flexGrow         1
  :text-align       "center"
  :overflow         "auto"
  :position         "relative"
  :display          "flex"
  :flex-direction   "column"
})

(def workspace-content {
  :width      "900px"
  :height     "100%"
  :margin     "0 auto"
  :text-align "left"
  :padding    "5em 96px 30vh"
  :overflow   "auto"
})

(def study-buttons {
  :background      "white"
  :width           "100%"
  :height          90
  :min-height      90
  :display         "flex"
  :justify-content "center"
  :align-items     "center"
  :position        "absolute"
  :bottom          0
  :left            0
  :right           0
})

(def topbar {
  :width           "100%"
  :height          45
  :min-height      45
  :display         "flex"
  :justify-content "space-between"
  :align-items     "center"
  :padding         "0 12px"
  :font-size       14
})

(def table-td {
  :border (str "1px solid " table-border)
})

(def text-input {
  :border 'none
  :outline 'none
  :height "21px"
})

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
  :width        (/ (- 900 32) field-count)
})

(def table-new-record {
  :display        'flex
  :align-items    'center
  :color          weak-color
  :border-bottom  border-weak
  :height         32
  :padding-left   8
  :padding-bottom 2
  :cursor         'pointer
})

(def table-cell {
  :position     "relative"
  :display      "flex"
  :align-items  "center"
  :white-space  "nowrap"
  :overflow     "hidden"
  :padding      "5px 8px 6px"
  :border-right border-weak
  :cursor       'pointer
})

(defn editing-table-cell [width] {
  :-webkit-user-modify 'read-write-plaintext-only
  :outline             0
  :position            "absolute"
  :top                 0
  :left                0
  :z-index             9
  :display             "felx"
  :background-color    workspace-background
  :padding             "5px 8px 6px"
  :border-right        border-weak
  :border-radius       "3px"
  :width               (max 240 width)
  :white-space         "pre-wrap"
  :word-break          "break-word"
  :-webkit-line-break  "after-white-space"
  :box-shadow          "rgba(84, 70, 35, 0.3) 0px 6px 20px,
                       rgba(84, 70, 35, 0.14) 0px 1px 3px,
                       rgba(0, 0, 0, 0.08) 0px 0px 1px"
})

(def add-field {
  :display   'flex
  :flex-grow 1
  :width     32
})

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
  :color           popout-button-foreground
  :box-shadow      "rgba(0, 0, 0, 0.08) 0px 0px 1px,
                    rgba(84, 70, 35, 0.14) 0px 1px 3px,
                    rgba(84, 70, 35, 0.2) 0px 1px 8px"
  :font-size 12
  :text-transform 'uppercase
})

(def content-editable {
  :outline 0
  :-webkit-user-modify "read-write-plaintext-only"
})

;; -- Typography -----------------------------------------------------------

(def h1 {
  :color          "rgb(34, 34, 34)"
  :font-weight    700
  :letter-spacing "-0.003em"
  :line-height    "1.1"
  :width          "100%"
  :margin-bottom  "0.25em"
  :font-size      "2.375em"
  :font-family    "-apple-system, BlinkMacSystemFont, \"Segoe UI\", Helvetica, \"Apple Color Emoji\", Arial, sans-serif, \"Segoe UI Emoji\", \"Segoe UI Symbol\""
})
