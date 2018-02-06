(ns leiningen.cidekick.db
  (:require [clojure.java.jdbc :as jdbc])
  (:import [com.zaxxer.hikari HikariConfig HikariDataSource]))

(defonce db-pool (atom nil))
(defonce db-env (atom {}))

(defn- create-db-pool [db-config]
  (let [port (Integer/parseInt (:port db-config))
        url (str "jdbc:"
                 (:driver db-config)
                 "://"
                 (:host db-config)
                 ":"
                 port
                 "/"
                 (:database db-config)
                 (when (:ssl db-config)
                   "?ssl=true"))

        config (doto (HikariConfig.)
                 (.setJdbcUrl url)
                 (.setUsername (:username db-config))
                 (.setPassword (:password db-config))
                 ;; Don't need a giant connection pool.
                 (.setMaximumPoolSize 2))

        datasource (HikariDataSource. config)]

    ;; Be polite to the DB: ensure pooled connections are closed on JVM exit.
    ;; Any exception in the callback is caught by the JVM and logged to stderr.
    (.addShutdownHook (java.lang.Runtime/getRuntime)
                      (Thread. (fn [] (.close datasource))))

    {:datasource datasource}))

(defn init! [db-config]
  (reset! db-env db-config)
  (when-let [old-pool @db-pool]
    (.close (:datasource old-pool)))
  (let [pool (create-db-pool db-config)]
    (reset! db-pool pool)))

(defmacro with-connection [conn & body]
  `(with-open [conn# (.getConnection (:datasource @db-pool))]
     (let [~conn {:connection conn#}]
       ~@body)))

(defn execute!
  ([sql]
   (execute! sql false))
  ([sql transaction?]
   (with-connection conn
     (jdbc/execute! conn sql :transaction? transaction?))))

(defn query! [sql]
  (with-connection conn
    (jdbc/query conn sql)))
