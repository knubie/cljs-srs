(defproject app "0.1.0"
  :dependencies [[org.clojure/clojure        "1.9.0"]
                 [org.clojure/test.check     "0.9.0"]
                 [org.clojure/clojurescript  "1.10.238"]
                 [reagent  "0.8.1"]
                 [cljstache "2.0.1"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljsjs/antd "3.5.0-0"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel  "0.5.16-SNAPSHOT"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev {:cljsbuild
                    {:builds
                      {:client {:source-paths ["test"]
                                :figwheel {:on-jsload "app.core/run"}
                                :compiler {:main "app.core"
                                           :asset-path "js"
                                           :optimizations :none
                                           :source-map true
                                           :source-map-timestamp true}}}}}
             :prod {:cljsbuild
                    {:builds
                      {:client {:compiler {:optimizations :advanced
                                           :elide-asserts true
                                           :pretty-print false}}}}}}
  :figwheel {:repl true}

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild {:builds
               {:client
                 {:source-paths ["src"]
                  :compiler     {:output-dir "resources/public/js"
                                 :output-to  "resources/public/js/main.js"}}}})
