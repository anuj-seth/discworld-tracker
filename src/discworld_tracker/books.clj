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
  (let [books (reduce (fn [acc m]
                        (let [volume-number (:volume-number m)]
                          (assoc acc volume-number (dissoc m :volume-number))))
                      {}
                      (read-edn-resource "books.edn"))
        user-data (if-let [user-data (user-data/load)]
                    user-data
                    {})]
    (map #(let [volume-number (:volume-number %1)
                read? (user-data volume-number false)]
            (assoc %1 :read? read?))
         books-list)))

(defstate books :start (load-books)
  :stop (println books))

(comment 

  (def m (map (fn [m]
                (let [volume-number (:volume-number m)]
                  (if (zero? (mod volume-number 5))
                    (assoc m :selected? true)
                    m)))
              (map (fn [m]
                     (let [volume-number (:volume-number m)]
                       (if (zero? (mod volume-number 2))
                         (assoc m :read? true)
                         m)))
                   (load-books))))

  (some #(= [true true] ((juxt :selected? :read?) %))
        m)

  (some #(= [true false] ((juxt :selected? :read?) %))
        m)
)

