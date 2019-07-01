(ns discworld-tracker.core
  (:require [mount.core :as mount]
            [discworld-tracker.tracker-ui :as tracker-ui])
  (:gen-class))

(defn -main
  "A personal read/not-read tracker for Terry Pratchett's Discworld series of novels"
  [& args]
  (mount/start)
  (tracker-ui/start-javafx)
  (mount/stop))


