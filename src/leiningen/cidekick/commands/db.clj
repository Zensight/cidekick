(ns leiningen.cidekick.commands.db
  (:require [clj-time.coerce :as cotime]
            [clj-time.core :as ctime]
            [clj-time.format :as ftime]
            [clojure.data.json :as json]
            [clojure.string :as cstr]
            [leiningen.cidekick.db :as db])
  (:import [java.util UUID]))

(def db-config-json-desc
  "Description of the :db-config-json argument. Used by doc strings."
  "A JSON string of database configuration with a scheme like:
  {\"database\": \"database-name\",
   \"driver\": \"jdbc-driver-to-use\",
   \"host\": \"host-name\",
   \"password\": \"database-password\",
   \"port\": \"port-number\",
   \"ssl\": \"require-ssl?\",
   \"username\": \"database-username\"}")

(def date-format (ftime/formatters :basic-date))

(defn parse-db-config-json [json-db-config]
  (json/read-str json-db-config :key-fn keyword))

(defn purge-old-dbs
  "Purge CI databases that are more than a week old."
  {}
  [project args]
  (let [opts (->> args
                  (apply hash-map)
                  (map (fn [[k v]] [(read-string k) v]))
                  (into {}))
        db-config-json (:db-config-json opts)]
    (if-not db-config-json
      (throw (RuntimeException. ":db-config-json argument required!"))
      (let [db-config (parse-db-config-json db-config-json)
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
         doall)))))

(defn stand-up-db
  {:doc (str "Stand up a new CI database with a random identifier.\n"
             "Options:\n"
             ":db-config-json [Required] - "
             db-config-json-desc)
   :help-arglists '([{:keys [db-config-json]}])}
  [project args]
  (let [opts (->> args
                  (apply hash-map)
                  (map (fn [[k v]] [(read-string k) v]))
                  (into {}))
        db-config-json (:db-config-json opts)]
    (if-not db-config-json
      (throw (RuntimeException. ":db-config-json argument required!"))
      (let [db-config (parse-db-config-json db-config-json)
            pg-db-config (assoc db-config :database "postgres")
            date (ftime/unparse date-format (ctime/now))
            uuid (-> (UUID/randomUUID)
                     .toString
                     (cstr/replace #"-" ""))
            db-name (cstr/join "_" [(:database db-config) date uuid])]
        (db/init! pg-db-config)
        (db/execute! [(str "create database " db-name)])
        (println (str "Stood up db:\n" db-name))))))

(defn tear-down-db
  "Tear down the CI database with the given identifier."
  {}
  [project args]
  (let [opts (->> args
                  (apply hash-map)
                  (map (fn [[k v]] [(read-string k) v]))
                  (into {}))
        {:keys [db-config-json db-name]} opts]
    (if-not db-config-json
      (throw (RuntimeException. ":db-config-json argument required!"))
      (if-not db-name
        (throw (RuntimeException. ":db-name argument required!"))
        (let [db-config (parse-db-config-json db-config-json)
              pg-db-config (assoc db-config :database "postgres")]
          (db/init! pg-db-config)
          (db/execute! [(str "drop database " db-name)])
          (println (str "Dropped database:\n" db-name)))))))
