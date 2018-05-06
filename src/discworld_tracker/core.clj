(ns discworld-tracker.core
  (:require [discworld-tracker.tracker-ui :as tracker-ui])
  (:gen-class))

(defn -main
  "A personal read/not-read tracker for Terry Pratchett's Discworld series of novels"
  [& args]
  (tracker-ui/start))
