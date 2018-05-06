(ns discworld-tracker.core
  (:require [discworld-tracker.tracker-ui :as tracker-ui])
  (:gen-class
   :extends javafx.application.Application))

(defn -start
  [app stage]
  (tracker-ui/start))

(defn -main
  "A personal read/not-read tracker for Terry Pratchett's Discworld series of novels"
  [& args]
  ;; passing the name of our class as the first argument to launch is mandatory.
  ;; if not passed, javafx gives an error that core/-main is not a subclass of javafx.application.Application
  (javafx.application.Application/launch discworld_tracker.core (into-array String args)))
