(ns discworld-tracker.tracker-ui
  (:require [fn-fx.fx-dom :as dom]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]])
  (:import (javafx.beans.property ReadOnlyObjectWrapper)))

(defn cell-value-factory
  [f]
  (reify javafx.util.Callback
    (call [this entity]
      (ReadOnlyObjectWrapper. (f (.getValue entity))))))

(defui TableColumn
  (render
   [this {:keys [key name max-width]}]
   (ui/table-column
    :text name
    ;;:min-width max-width
    :resizable true
    :cell-value-factory (cell-value-factory #(key %)))))

(defn render-table
  [books text-label data-filter-fn]
  (let [top-label (ui/h-box
                   :alignment :center
                   :children [(ui/label
                               :text text-label
                               :font (ui/font
                                      :family "Tahoma"
                                      :weight :normal
                                      :size 20))])
        books-table (ui/table-view
                     :items (filter data-filter-fn books)
                     :min-width 500
                     :v-box/vgrow javafx.scene.layout.Priority/ALWAYS
                     :columns (map (fn [[key header max-width]]
                                     (table-column {:key key
                                                    :name header
                                                    :max-width max-width}))
                                   [[:volume-number "Vol. #" 5]
                                    [:title "Title" 100]
                                    [:year-published "Publication Year" 5]
                                    [:subseries "Subseries" 10]]))]
    (ui/v-box
     :spacing 10
     :children [top-label
                books-table])))

(defui ReadBooks
  (render
   [this books]
   (render-table books
                 "BOOKS ALREADY READ"
                 :read?)))

(defui MoveControls
  (render
   [this args]
   (ui/v-box
    :alignment :center
    :spacing 5
    :children [(ui/button
                :style "-fx-base: rgb(30, 30, 35);"
                :text "->")
               (ui/button
                :text "<-")])))

(defui NotReadBooks
  (render
   [this books]
   (render-table books
                 "BOOKS NOT READ"
                 (comp not :read?))))

(defui BooksView
  (render
   [this books]
   (ui/h-box
    :spacing 10
    ;;:style "-fx-base: rgb(30, 30, 35);"
    :children [(not-read-books books)
               (move-controls)
               (read-books books)])))
(defui Stage
  (render
   [this args]
   (ui/stage
    :title "Discworld Tracker"
    :maximized true
    :shown true
    :scene (ui/scene
            :root (books-view args)))))

(defn start
  []
  (let [data-state (atom (clojure.edn/read-string (slurp (clojure.java.io/resource "books.edn"))))
        handler-fn (fn [args])
        ui-state (agent (dom/app (stage @data-state) handler-fn))]
    (add-watch data-state
               :ui
               (fn [_ _ _]
                 (send ui-state
                       (fn [old-ui]
                         (dom/update-app
                          old-ui
                          (stage @data-state))))))))
