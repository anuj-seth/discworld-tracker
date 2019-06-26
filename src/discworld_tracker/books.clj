(ns discworld-tracker.books
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as spec]
            [discworld-tracker.user-data :as user-data]))

(defn read-resource
  [resource-file-name]
  (slurp (io/resource
          resource-file-name)))

(defn read-edn-resource
  [resource-file-name]
  (edn/read-string (read-resource
                    resource-file-name)))

(defn load-books
  "Loads the list of books from the reference
  EDN file and merges user data of books already read."
  []
  (let [books-list (read-edn-resource "books.edn")
        user-data (if-let [user-data (user-data/load)]
                    user-data
                    {})]
    (map #(let [volume-number (:volume-number %1)
                read? (user-data volume-number false)]
            (assoc %1 :read? read?))
         books-list)))

(defstate books :start (load-books)
  :stop (println books))
