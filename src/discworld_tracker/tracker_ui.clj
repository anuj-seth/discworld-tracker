(ns discworld-tracker.tracker-ui
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [fn-fx.fx-dom :as dom]
            [fn-fx.controls :as ui]
            [fn-fx.diff :refer [component defui render should-update?]]
            [fn-fx.render-core :as render-core]
            [discworld-tracker.app-state :refer [discworld-app-state]]
            [discworld-tracker.event-handler :as event-handler])
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
   [this {:keys [key name display-fn]}]
   (ui/table-column :text name
                    :resizable true
                    :cell-value-factory (cell-value-factory #(display-fn
                                                              (key %))))))

(defn render-table
  [books text-label select-identifier]
  (let [top-label (ui/h-box :alignment :center
                            :children [(ui/label :text text-label
                                                 :font (ui/font :family "Tahoma"
                                                                :weight :normal
                                                                :size 20))])
        books-table (ui/table-view :items (sort-by :volume-number
                                                   <
                                                   books)
                                   :placeholder (ui/label :text
                                                          "No books in this table")
                                   ;; javfx will allocate equal widths to all columns
                                   :column-resize-policy javafx.scene.control.TableView/CONSTRAINED_RESIZE_POLICY
                                   ;; to make the table grow when the enclosing container is resized
                                   :v-box/vgrow javafx.scene.layout.Priority/ALWAYS
                                   :listen/selection-model.selected-item {:event select-identifier}
                                   :columns (map (fn [[key header display-fn]]
                                                   (table-column {:key key
                                                                  :name header
                                                                  :display-fn display-fn}))
                                                 [[:volume-number "Vol. #"  identity]
                                                  [:title "Title" identity]
                                                  [:year-published "Publication Year" identity]
                                                  [:subseries "Subseries" name]]))]
    (ui/v-box :spacing 10
              ;; to make the table grow horizontally when the enclosing container is resized
              :h-box/hgrow javafx.scene.layout.Priority/ALWAYS
              :children [top-label
                         books-table])))


(defui ReadBooks
  (render
   [this {:keys [books already-read]}]
   (render-table (filter #(already-read (:volume-number %))
                         books)
                 "BOOKS ALREADY READ"
                 :read-book-selected)))
(comment 
  ;;(author here), So for a inline style you can use a string on the style field of the component.

  ;;This blog[http://nils-blum-oeste.net/javafx-style-using-clojure-fn-fx-garden-desktop-application-design/] goes into a bit more detail, but for stylesheets you should be able to say :stylesheet "main.css" on a scene and assuming main.css works, you should be fine.

  ;;Fn-fx is highly dynamic, you can overload the convert-value method (https://github.com/halgari/fn-fx/blob/master/src/fn_fx/render_core.clj#L31-L32) and provide a custom converter.

  ;;So let's say you want to do something crazy like support integers as stylesheets. If you call :style 1 you'll get an error stating there is no override for convert-value from int to string. Then you simply write

  (defmethod convert-value [Long String]
    [long-value _]
    ..conversion logic here should return a string...)
  )

(defn image
  [image-file]
  (let [image-value-tp (ui/image :is (io/input-stream
                                      (io/resource image-file)))
        image (render-core/convert-value image-value-tp
                                         javafx.scene.image.Image)]
    image))

(defui MoveControls
  (render
   [this {:keys [books already-read read-selected unread-selected]}]
   (let [move-to-read-btn-disabled? (empty? unread-selected)
         move-to-unread-btn-disabled? (empty? read-selected)]
     (ui/v-box :alignment :center
               :spacing 5
               :children [(ui/button
                           ;;:style "-fx-base: rgb(30, 30, 35);"
                           :graphic (ui/image-view :image (image "right_arrow.png")
                                                   :fit-height 20
                                                   :fit-width 20)
                           :disable move-to-read-btn-disabled?
                           :on-action {:event :move-to-read})
                          (ui/button 
                           :graphic (ui/image-view :image (image "left_arrow.png")
                                                   :fit-height 20
                                                   :fit-width 20)
                           :disable move-to-unread-btn-disabled?
                           :on-action {:event :move-to-unread})]))))

(defui NotReadBooks
  (render
   [this {:keys [books already-read]}]
   (render-table (filter (complement #(already-read (:volume-number %)))
                         books)
                 "BOOKS NOT READ"
                 :unread-book-selected)))


(defui BooksView
  (render
   [this data]
   (ui/h-box :spacing 10
             ;;:style "-fx-base: rgb(30, 30, 35);"
             :children [(not-read-books data)
                        (move-controls data)
                        (read-books data)])))

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
   (let [image-value-tp (ui/image :is (io/input-stream
                                       (io/resource "Watch-Crest.png")))
         image (render-core/convert-value image-value-tp
                                          javafx.scene.image.Image)]
     (ui/stage :title "Discworld Tracker"
               :on-close-request (force-exit)
               :maximized true
               :icons [image]
               :shown true
               :scene (ui/scene :root (books-view args))))))


(defn -start
  [& args]
  (let [ui-state (agent (dom/app (the-stage @discworld-app-state)
                                 (event-handler/get-handler-fn discworld-app-state)))]
    (add-watch discworld-app-state
               :ui (fn [_ _ old-state new-state]
                     (send ui-state
                           (fn [old-ui]
                             (dom/update-app old-ui
                                             (the-stage new-state))))))))

(defn start-javafx
  [& args]
  (javafx.application.Application/launch discworld_tracker.tracker_ui
                                         (into-array String args)))



