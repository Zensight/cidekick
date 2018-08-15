(ns leiningen.cidekick
  (:require [leiningen.cidekick.commands.db :as db]
            [leiningen.core.main :as lein]))

(def sub-tasks
  "Plugin commands."
  {"print-config" db/print-config
   "purge-old-dbs" db/purge-old-dbs
   "stand-up-db" db/stand-up-db
   "tear-down-db" db/tear-down-db})

(defn cidekick
  "CI sidekick. Various CI helper functions."
  {:subtasks [#'db/print-config
              #'db/purge-old-dbs
              #'db/stand-up-db
              #'db/tear-down-db]}
  [project & [sub-task & args]]
  (if-let [task (sub-tasks sub-task)]
    (task project args)
    (lein/warn (str "Unknown sub-task `lein cidekick " sub-task "`"))))
