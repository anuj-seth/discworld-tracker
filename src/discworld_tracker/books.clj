(ns discworld-tracker.books
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as spec]))

(defn read-resource
  [resource-file-name]
  (slurp (io/resource resource-file-name)))

(defn read-edn-resource
  [resource-file-name]
  (edn/read-string (read-resource resource-file-name)))

(defn sort-by-volume-number
  [coll]
  (sort-by :volume-number
           <
           coll))

(defn load-user-data
  []
  nil)

(defn load-books
  "This function loads the list of books from the reference
  EDN file and merges that with user data on which books have already
  been read."
  []
  (let [books-list (sort-by-volume-number (read-edn-resource
                                           "books.edn"))
        user-data (if-let [user-data (load-user-data)]
                    (sort-by-volume-number user-data)
                    (map #(hash-map :volume-number %, :read? false)
                         (iterate inc 1)))]
    (map #(merge %1 %2)
         books-list
         user-data)))

(defstate books :start (load-books)
  :stop (println books))
