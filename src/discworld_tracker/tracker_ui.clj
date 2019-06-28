(ns discworld-tracker.tracker-ui
  (:require [fn-fx.fx-dom :as dom]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]]
            [discworld-tracker.books :refer [books]])
  (:import (javafx.beans.property ReadOnlyObjectWrapper))
  (:gen-class :extends
              javafx.application.Application))

(defn cell-value-factory
  [f]
  (reify javafx.util.Callback
    (call [this entity]
      (ReadOnlyObjectWrapper. (f (.getValue entity))))))

(defui TableColumn
  (render
   [this {:keys [key name max-width]}]
   (ui/table-column :text name
                    ;;:min-width max-width
                    ;;:resizable true
                    :cell-value-factory (cell-value-factory #(key %)))))

(defn render-table
  [books text-label data-filter-fn]
  (let [sort-by-volume-number (fn [coll]
                                (sort-by :volume-number
                                         <
                                         coll))
        top-label (ui/h-box :alignment :center
                            :children [(ui/label :text text-label
                                                 :font (ui/font :family "Tahoma"
                                                                :weight :normal
                                                                :size 20))])
        books-table (ui/table-view :items (sort-by-volume-number
                                           (filter data-filter-fn books))
                                   ;;:min-width 500
                                   :column-resize-policy javafx.scene.control.TableView/UNCONSTRAINED_RESIZE_POLICY
                                   ;; to make the table grow when the enclosing container is resized
                                   :v-box/vgrow javafx.scene.layout.Priority/ALWAYS
                                   :listen/selection-model.selected-item {:event :row-selected}
                                   :columns (map (fn [[key header max-width]]
                                                   (table-column {:key key
                                                                  :name header
                                                                  :max-width max-width}))
                                                 [[:volume-number "Vol. #" 5]
                                                  [:title "Title" 100]
                                                  [:year-published "Publication Year" 5]
                                                  [:subseries "Subseries" 10]]))]
    (ui/v-box :spacing 10
              ;; to make the table grow horizontally when the enclosing container is resized
              :h-box/hgrow javafx.scene.layout.Priority/ALWAYS
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
   [this books]
   (ui/v-box :alignment :center
             :spacing 5
             :children [(ui/button ;;:style "-fx-base: rgb(30, 30, 35);"
                                   :text "->"
                                   :disable (not-any? #(= [true false] (juxt :selected? :read? %)) books)
                                   :on-action {:event :move-to-read})
                        (ui/button :text "<-"
                                   :disable (not-any? #(= [true true] (juxt :selected? :read? %)) books)
                                   :on-action {:event :move-to-unread})])))

(defui NotReadBooks
  (render
   [this books]
   (render-table books
                 "BOOKS NOT READ"
                 (complement :read?))))


(defui BooksView
  (render
   [this books]
   (ui/h-box :spacing 10
             ;;:style "-fx-base: rgb(30, 30, 35);"
             :children [(not-read-books books)
                        (move-controls books)
                        (read-books books)])))

(defn force-exit
  []
  (reify javafx.event.EventHandler
    (handle [this event]
      ;; this is required to shutdown the fn-fx agent that
      ;; handles the app.
      ;; without the shutdown-agents it takes a minute for the
      ;; agent to shut down.
      (shutdown-agents)
      (javafx.application.Platform/exit))))

(defui TheStage
  (render
   [this args]
   (ui/stage :title "Discworld Tracker"
             :on-close-request (force-exit)
             :maximized true
             :shown true
             :scene (ui/scene :root (books-view args)))))


(defn -start
  [& args]
  (let [data-state (atom books)
        handler-fn (fn [{:keys [event] :as event-data}]
                     (condp = event
                       :move-to-read (println "move to read button pressed")
                       :move-to-unread (println "move to un-read button pressed")
                       :row-selected (let [{:keys [fn-fx.listen/new fn-fx.listen/old]} event-data]
                                       (swap! data-state (fn [l]
                                                           (map #(if )l)))
                                       (println old new))
                       (println "something happened" event-data)))
        ui-state (agent (dom/app (the-stage @data-state)
                                 handler-fn))]
    (add-watch data-state
               :ui (fn [_ _ _]
                     (send ui-state
                           (fn [old-ui]
                             (dom/update-app old-ui
                                             (the-stage @data-state))))))))

(defn start-javafx
  [& args]
  (javafx.application.Application/launch discworld_tracker.tracker_ui
                                         (into-array String args)))



