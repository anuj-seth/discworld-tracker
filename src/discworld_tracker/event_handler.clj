(ns discworld-tracker.event-handler
  (:require [clojure.set :as set]))

(defn handle-select-event
  [data-state selection-key event-data]
  (let [{:keys [fn-fx.listen/new fn-fx.listen/old]} event-data]
    (println selection-key old new)
    (if (nil? new)
      ;; unselect event
      ;; remove
      (let [to-remove (:volume-number old)]
        (swap! data-state
               update
               selection-key
               set/difference #{to-remove}))
      (let [to-add (:volume-number new)]
        (swap! data-state
               update
               selection-key conj to-add)))))

(defn get-handler-fn
  [data-state]
  (fn handler-fn [{:keys [event] :as event-data}]
    (condp = event
      :move-to-read (let [selection (:unread-selected @data-state)]
                      (swap! data-state
                             update
                             :already-read
                             set/union
                             selection))
      :move-to-unread (let [selection (:read-selected @data-state)]
                        (swap! data-state
                               update
                               :already-read
                               set/difference
                               selection))
      :read-book-selected (handle-select-event data-state :read-selected event-data)
      :unread-book-selected (handle-select-event data-state :unread-selected event-data)
      (println "something happened" event-data))))
