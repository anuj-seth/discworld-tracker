(ns discworld-tracker.app-state
  (:require [mount.core :refer [defstate]]
            [discworld-tracker.util :as util]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def app-folder (str (System/getProperty "user.home")
                     "/"
                     ".discworld-tracker"))

(def user-data-filename (str app-folder
                             "/"
                             "user_data"))

(defn load-books
  "Loads the list of books from the reference
  EDN file and merges user data of books already read."
  []
  (util/read-edn-resource "books.edn"))

(defn load-user-data
  []
  (if (.exists (io/as-file user-data-filename))
    (edn/read-string (slurp user-data-filename))
    {}))

(defn dump-user-data
  [app-state]
  (let [already-read (:already-read app-state)
        all-books (:books app-state)]
    (io/make-parents user-data-filename)
    (spit user-data-filename
          (pr-str (filter #(already-read (:volume-number %))
                          all-books)))))

(defn app-state
  []
  (let [books (load-books)
        user-data (load-user-data)
        already-read (into #{} (map :volume-number user-data))]
    (atom {:books books
           :user-data user-data
           :already-read already-read
           :read-selected #{}
           :unread-selected #{}})))

(defstate discworld-app-state
  :start (app-state)
  :stop (dump-user-data @discworld-app-state))
