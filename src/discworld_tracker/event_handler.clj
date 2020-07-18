(ns discworld-tracker.event-handler
  (:require [clojure.set :as set]))

(defmulti select-event
  (fn [app-data selected-what {:keys [fn-fx.listen/new fn-fx.listen/old]}]
    [(nil? new) (nil? old)]))

(defmethod select-event [true false]
  remove-old-selection
  [app-data selected-what {:keys [fn-fx.listen/old]}]
  (let [to-remove (:volume-number old)]
    (swap! app-data
           update
           selected-what
           set/difference
           #{to-remove})))

(defmethod select-event [false true]
  add-new-selection
  [app-data selected-what {:keys [fn-fx.listen/new]}]
  (let [to-add (:volume-number new)]
    (swap! app-data
           update
           selected-what
           conj
           to-add)))

(defmethod select-event [false false]
  add-new-remove-old-selection
  [app-data selected-what {:keys [fn-fx.listen/new fn-fx.listen/old]}]
  (let [to-add (:volume-number new)
        to-remove (:volume-number old)]
    (swap! app-data
           update
           selected-what
           conj
           to-add)
    (swap! app-data
           update
           selected-what
           set/difference
           #{to-remove})))

(defmethod select-event :default
  do-nothing-op
  [app-data selected-what {:keys [fn-fx.listen/new fn-fx.listen/old]}]
  (println "this should never be called" old new))

(defn get-handler-fn
  [data-state]
  (let [update-atom! (fn [datom update-key update-fn compute-value-fn]
                       (swap! data-state
                              update
                              update-key
                              update-fn
                              (comput-value-fn @data-state)))]
    (fn handler-fn [{:keys [event] :as event-data}]
      (condp = event
        :move-to-read (update-atom! data-state
                                    :already-read
                                    set/union
                                    :unread-selected)
        :move-to-unread (update-atom! data-state
                                      :already-read
                                      set/difference
                                      :read-selected)
        :read-book-selected (select-event data-state :read-selected event-data)
        :unread-book-selected (select-event data-state :unread-selected event-data)
        (println "something happened" event-data)))))
