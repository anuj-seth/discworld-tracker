(defproject discworld-tracker "0.1.0-SNAPSHOT"
  :description "A personal read/not-read tracker for Terry Pratchett's Discworld series of novels."
  :url "https://github.com/anuj-seth/discworld-tracker"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [fn-fx/fn-fx-openjfx11 "0.5.0-SNAPSHOT"]
                 [mount "0.1.14"]]
  :main ^:skip-aot discworld-tracker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :java-cmd "/usr/lib/jvm/java-11-openjdk-amd64/bin/java")
