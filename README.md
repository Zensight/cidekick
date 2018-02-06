# CIdekick

A Leiningen plugin to do many wonderful things to embetter your CI experience.

## Usage

Put `[cidekick "0.1.0"]` into the `:plugins` vector of your project.clj.

Stand up a test database:

    $ lein cidekick stand-up-db :db-config-json '{"database": "your-db"}'

Tear down a test database:

    $ lein cidekick tear-down-db :db-config-json '{"database": "your-db"}' :db-name $DB_TEAR_DOWN

Purge old test databases more than 1 week old

    $ lein cidekick purge-old-dbs :db-config-json '{"database": "your-db"}'

## License

Copyright © 2018 Zensight

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
