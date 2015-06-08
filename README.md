# cledgers

A ledgers app for personal financial budgeting purposes

## Technical Description

A clojure and clojurescript application using

* Reagent (react) for client side rendering
* Low-latency web connection using Websockets with fallback as provided in httpkit
* Stuart Sierra's component for easy dev reloading

### in the future

* Datomic

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein run

### Running from the repl

    (ns user)
    (go)

 * Stop: `(stop)`
 * Reload: `(reset)`

### Clojurescript compiling
* Open a terminal tab
* cd to cledgers
* $`lein cljsbuild auto`

This will compile after changes are made to cljs files

### Watching logs
I think the serverside logs might show up in the repl, but if not you can also:

* cd to cledgers
* $`tail -f cledgers.log`

### View the app
navigate to http://localhost:8080


## License

Copyright © 2015 Frank Henard

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
