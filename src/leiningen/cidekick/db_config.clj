(ns leiningen.cidekick.db-config
  (:require [clojure.data.json :as json]
            [clojure.string :as cstr]))

(def help-arglists
  '([{:keys [config-json database host password port ssl username]}]))

(def config-json-desc
  "Description of the :config-json argument. Used by doc strings."
  "A JSON string of database configuration with a scheme like:
  {\"database\": \"database-name\",
   \"host\": \"host-name\",
   \"password\": \"database-password\",
   \"port\": \"port-number\",
   \"ssl\": \"require-ssl?\",
   \"username\": \"database-username\"}")

(def individual-args-desc
  "Description of individual db config arguments. Used by doc strings."
  ":database - The database name. Required, though the value may come from :config-json arg.
   :host - The database host name. Required, though the value may come from :config-json arg.
   :password - The database password. Required, though the value may come from :config-json arg.
   :port - The database port. Required, though the value may come from :config-json arg.
   :ssl - Boolean specifying whether or not SSL should be used. Required, though the value may come from :config-json arg.
   :username - The database user. Required, though the value may come from :config-json arg.")

(def config-desc
  "The configuration options description. Used by doc strings."
  (str "Options:\n"
       ":config-json [Optional] - "
       config-json-desc
       "\n"
       individual-args-desc))
(def config-json-desc
  "Description of the :config-json argument. Used by doc strings."
  "A JSON string of database configuration with a scheme like:
  {\"database\": \"database-name\",
   \"host\": \"host-name\",
   \"password\": \"database-password\",
   \"port\": \"port-number\",
   \"ssl\": \"require-ssl?\",
   \"username\": \"database-username\"}")

(def individual-args-desc
  "Description of individual db config arguments. Used by doc strings."
  (cstr/join
   "\n"
   [":database - The database name. Required, though the value may come from :config-json arg."
    ":host - The database host name. Required, though the value may come from :config-json arg."
    ":password - The database password. Required, though the value may come from :config-json arg."
    ":port - The database port. Required, though the value may come from :config-json arg."
    ":ssl - Boolean specifying whether or not SSL should be used. Required, though the value may come from :config-json arg."
    ":username - The database user. Required, though the value may come from :config-json arg."]))

(def arguments-desc
  "The configuration options description. Used by doc strings."
  (str "Options:\n"
       ":config-json [Optional] - "
       config-json-desc
       "\n"
       individual-args-desc))

(defn parse-config-json [config-json]
  (json/read-str config-json :key-fn keyword))

(defn args->db-config [args]
  (let [opts (->> args
                  (apply hash-map)
                  (map (fn [[k v]] [(read-string k) v]))
                  (into {}))
        config-json (some-> opts :config-json parse-config-json)
        individual-configs (cond-> {:driver "postgresql"}
                             (:database opts)
                             (assoc :database (:database opts))
                             (:host opts)
                             (assoc :host (:host opts))
                             (:password opts)
                             (assoc :password (:password opts))
                             (:port opts)
                             (assoc :port (:port opts))
                             (not (nil? (:ssl opts)))
                             (assoc :ssl (:ssl opts))
                             (:username opts)
                             (assoc :username (:username opts)))
        config (-> config-json
                   (merge individual-configs)
                   (update :ssl boolean))]
      (when-not (every? some? ((juxt :database :host :password :port :ssl :username) config))
        (throw (RuntimeException. (str "The combination of the :config-json argument and the "
                                       "individual :database, :host, :password, :port, :ssl, and "
                                       ":username arguments must provide values for all of the "
                                       "individual arguments. Invalid config: \n"
                                       config))))
      config))
