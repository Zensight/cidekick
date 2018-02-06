(defproject co.zensight/cidekick "0.1.0-SNAPSHOT"
  :description "CI Sidekick: Not a cider nrepl plugin"
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :url "https://github.com/Zensight/cidekick"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [
                 ;; duh
                 [org.clojure/clojure "1.9.0"]

                 ;;joda time wrapper
                 [clj-time "0.14.2"]

                 ;; json
                 [org.clojure/data.json "0.2.5"]

                 ;; database access
                 [org.clojure/java.jdbc "0.3.5"]

                 ;; postgresql driver for JDBC
                 [org.postgresql/postgresql "42.2.1"]

                 ;; database connection pool
                 [com.zaxxer/HikariCP "2.7.2"]])
