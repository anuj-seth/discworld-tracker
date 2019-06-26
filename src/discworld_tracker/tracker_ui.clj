(ns discworld-tracker.tracker-ui
  (:require [fn-fx.fx-dom :as dom]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]]
            [discworld-tracker.books :refer [books]])
  (:import (javafx.beans.property ReadOnlyObjectWrapper)
           (javafx.beans.value ObservableValue)
           (javafx.beans.value ChangeListener))
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
                                   :id :my-id
                                   ;;:min-width 500
                                   :column-resize-policy javafx.scene.control.TableView/UNCONSTRAINED_RESIZE_POLICY
                                   ;; to make the table grow when the enclosing container is resized
                                   :v-box/vgrow javafx.scene.layout.Priority/ALWAYS
                                   :listen/selection-model.selected-item {:event :row-selected
                                                                          :fn-fx/include {:my-id [:selection-model.selected-item.value]}
                                                                          }
                                   :columns (map (fn [[key header max-width]]
                                                   (table-column {:key key
                                                                  :name header
                                                                  :max-width max-width}))
                                                 [[:volume-number "Vol. #" 5]
                                                  [:title "Title" 100]
                                                  [:year-published "Publication Year" 5]
                                                  [:subseries "Subseries" 10]]))]
    (println (type books-table))
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
   [this [move-to-read-btn-state move-to-unread-btn-state]]
   (println move-to-read-btn-state move-to-unread-btn-state)
   (ui/v-box :alignment :center
             :spacing 5
             :children [(ui/button ;;:style "-fx-base: rgb(30, 30, 35);"
                                   :text "->"
                                   :disable (not= :enabled move-to-read-btn-state)
                                   :on-action {:event :move-to-read-btn-pressed})
                        (ui/button :text "<-"
                                   :disable (not= :enabled move-to-unread-btn-state)
                                   :on-action {:event :move-to-unread-btn-pressed})])))

(defui NotReadBooks
  (render
   [this books]
   (render-table books
                 "BOOKS NOT READ"
                 (complement :read?))))


(defui BooksView
  (render
   [this {:keys [books move-to-read-btn-state move-to-unread-btn-state]}]
   (let [not-read-books-view (not-read-books books)
         read-books-view (read-books books)]
     (ui/h-box :spacing 10
               ;;:style "-fx-base: rgb(30, 30, 35);"
               :children [not-read-books-view
                          (move-controls [move-to-read-btn-state move-to-unread-btn-state])
                          ;;read-books-view
                          ]))))

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
  (let [data-state (atom {:books books
                          :move-to-read-btn-state :enabled 
                          :move-to-unread-btn-state :enabled})
        handler-fn (fn [{:keys [event] :as event-data}]
                     (condp = event
                       :move-to-read-btn-pressed (println "move to read button pressed")
                       :move-to-unread-btn-pressed (println "move to un-read button pressed")
                       (println "something selected" event-data)))
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

(comment 
  (defprotocol Person
    (full-name [this])
    (greeting [this msg])
    (description [this]))

  (defrecord FictionalCharacter [name appears-in author]
    Person
    (full-name [this] (:name this))
    (greeting [this msg] (str msg " " (:name this)))
    (description [this]
      (str (:name this) " is a character in " (:appears-in this))))

  (defrecord Employee [first-name last-name department]
    Person
    (full-name [this] (str first-name " " last-name))
    (greeting [this msg] (str msg " " first-name))
    (description [this]
      (str (:first-name this) " works in " (:department this))))

  (def elizabeth (map->FictionalCharacter {:name "Elizabeth Bennet" :appears-in "Pride & Prejudice" :author "Austen"}))

  (greeting elizabeth "helo ")
  (def watson (->FictionalCharacter "John Watson" "Sign of four" "Arthur Conan Doyle"))
  (full-name watson))
