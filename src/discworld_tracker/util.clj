(ns discworld-tracker.util
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn read-resource
  [resource-file-name]
  (slurp (io/resource
          resource-file-name)))

(defn read-edn-resource
  [resource-file-name]
  (edn/read-string (read-resource
                    resource-file-name)))
