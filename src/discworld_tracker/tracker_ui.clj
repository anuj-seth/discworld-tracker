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

(defui BookView
  (render [this books]
          (ui/table-view
           :items books
           :columns (map (fn [key header]
                           (table-column {:key key
                                          :name header}))
                         [:volume-number :title :year-published :subseries :notes]
                         ["#" "Title" "Publication Year" "Subseries" "Notes"]))))

(defui Stage
  (render [this args]
          (ui/stage
           :title "Hello World!"
           :shown true
           :scene (ui/scene
                   :root (book-view args)
                   ))))

(defn start
  []
  (let [data-state (atom (clojure.edn/read-string (slurp (clojure.java.io/resource "books.edn"))))
        handler-fn (fn [args])
        ui-state (agent (dom/app (stage @data-state) handler-fn))]
    (add-watch data-state :ui (fn [_ _ _]
                                (send ui-state
                                      (fn [old-ui]
                                        (dom/update-app old-ui (stage @data-state))))))))
