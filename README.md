# CIdekick

A Leiningen plugin to do many wonderful things to embetter your CI experience.

## Usage

Put `[cidekick "1.0.0"]` into the `:plugins` vector of your project.clj.

Stand up a test database:

    $ lein cidekick stand-up-db :db-config-json '{"database": "db-base-name", "host": "psql.mydomain.com", "port": 5432}'

    # OR

    $ lein cidekick stand-up-db :database cidekick_test :host psql.mydomain.com :password $DB_PASS :port 5432 :ssl true :username postgres

Tear down a test database:

    $ lein cidekick tear-down-db :db-config-json '{"host": "your-db-host", "database": "cidekick_test_20180815_up_9e4567dc90e248168b1d3251d4e0aef1"}'

    # OR

    $ lein cidekick tear-down-db :database cidekick_test_20180815_up_9e4567dc90e248168b1d3251d4e0aef1 :host psql.mydomain.com :password $DB_PASS :port 5432 :ssl true :username postgres

Purge old test databases more than 1 week old

    $ lein cidekick purge-old-dbs :db-config-json '{"database": "your-db"}'

    $ lein cidekick purge-old-dbs :database db-base-name :host psql.mydomain.com :password $DB_PASS :port 5432 :ssl true :username postgres

## License

Copyright © 2018 Zensight

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
