(ns leiningen.cidekick.commands.db
  (:require [clj-time.coerce :as cotime]
            [clj-time.core :as ctime]
            [clj-time.format :as ftime]
            [clojure.pprint :as pprint]
            [clojure.string :as cstr]
            [leiningen.cidekick.db-config :as config]
            [leiningen.cidekick.db :as db])
  (:import [java.util UUID]))

(def date-format (ftime/formatters :basic-date))

(defn print-config
  {:doc (str "Prints the given configuration or throws if the given configuration is invalid.\n"
             config/arguments-desc)
   :help-arglists config/help-arglists}
  [project args]
  (pprint/pprint (config/args->db-config args)))


(defn purge-old-dbs
  {:doc (str "Purge CI databases that are more than a week old.\n"
             config/arguments-desc
             "\n\nFor this command the :database argument identifies the base database name that "
             "should be used to match databases that can be purged.")
   :help-arglists config/help-arglists}
  [project args]
  (let [db-config (config/args->db-config args)
        base-db-name (:database db-config)
        pg-db-config (assoc db-config :database "postgres")
        today (ctime/with-time-at-start-of-day (ctime/now))
        cut-off (ctime/minus today (ctime/days 6))]
    (db/init! pg-db-config)
    (->>
     (db/query! ["SELECT datname FROM pg_database WHERE datistemplate = false and datname ~ ?"
                 (str "^" base-db-name "_[0-9]{8}_[0-9a-f]{32}$")])
     (map :datname)
     (remove (fn [db-name]
               (->> db-name
                    (re-find (re-pattern (str "^" base-db-name "_([0-9]{8})_[0-9a-f]{32}")))
                    last
                    (ftime/parse date-format)
                    (ctime/before? cut-off))))
     (map (fn [db-name]
            (db/execute! [(str "drop database " db-name)])
            (println "Dropped database" db-name)))
     doall)))

(defn stand-up-db
  {:doc (str "Stand up a new CI database with a random identifier.\n"
             config/arguments-desc
             "\n\nFor this command the :database argument identifies the base database name that "
             "should be used when generating new databases.")
   :help-arglists config/help-arglists}
  [project args]
  (let [db-config (config/args->db-config args)
        pg-db-config (assoc db-config :database "postgres")
        date (ftime/unparse date-format (ctime/now))
        uuid (-> (UUID/randomUUID)
                 .toString
                 (cstr/replace #"-" ""))
        db-name (cstr/join "_" [(:database db-config) date uuid])]
    (db/init! pg-db-config)
    (db/execute! [(str "create database " db-name)])
    (println (str "Stood up db:\n" db-name))))

(defn tear-down-db
  {:doc (str "Tear down the CI database with the given identifier.\n"
             config/arguments-desc
             "\n\nFor this command the :database argument identifies the exact name of a database "
             "that should be removed.")
   :help-arglists config/help-arglists}
  [project args]
  (let [db-config (config/args->db-config args)
        db-name (:database db-config)
        pg-db-config (assoc db-config :database "postgres")]
    (db/init! pg-db-config)
    (db/execute! [(str "drop database " db-name)])
    (println (str "Dropped database:\n" db-name))))
