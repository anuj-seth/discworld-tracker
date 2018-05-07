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
   [this {:keys [key name]}]
   (ui/table-column
    :text name
    :cell-value-factory (cell-value-factory #(key %)))))

(defui ReadBooks
  (render [this books]
          (ui/v-box
           :spacing 10
           :children [(ui/label
                       :text "BOOKS ALREADY READ"
                       :alignment :center
                       :font (ui/font
                              :family "Tahoma"
                              :weight :normal
                              :size 20))
                      (ui/table-view
                       :items (filter :read? books)
                       :columns (map (fn [key header]
                                       (table-column {:key key
                                                      :name header}))
                                     [:volume-number :title :year-published :subseries :notes]
                                     ["#" "Title" "Publication Year" "Subseries" "Notes"]))])))

(defui MoveControls
  (render [this args]
          (ui/v-box
           :alignment :center
           :spacing 5
           :children [(ui/button
                       :text "->")
                      (ui/button
                       :text "<-")])))

(defui NotReadBooks
  (render [this books]
          (ui/v-box
           :spacing 10
           :children [(ui/label
                       :text "BOOKS NOT READ"
                       :font (ui/font
                              :family "Tahoma"
                              :weight :normal
                              :size 20))
                      (ui/table-view
                       :items (filter (comp not :read?) books)
                       :columns (map (fn [key header]
                                       (table-column {:key key
                                                      :name header}))
                                     [:volume-number :title :year-published :subseries :notes]
                                     ["#" "Title" "Publication Year" "Subseries" "Notes"]))])))

(defui BooksView
  (render [this books]
          (ui/h-box
           :spacing 10
           ;;:style "-fx-base: rgb(30, 30, 35);"
           :children [(not-read-books books)
                      (move-controls)
                      (read-books books)])))
(defui Stage
  (render [this args]
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
    (add-watch data-state :ui (fn [_ _ _]
                                (send ui-state
                                      (fn [old-ui]
                                        (dom/update-app old-ui (stage @data-state))))))))
