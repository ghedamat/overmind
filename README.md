# Overmind

## What is this?

This is mainly an experiment with clojure [components](https://github.com/stuartsierra/component).

Overmind is a server monitoring tool, input components can send notifications, output components can listen to notifications and re-broadcast them to external systems.

### Current inputs

* a path watcher that lets you know when a file is added/removed from a directory tree
* a space watcher that sends event when you're running out of space

### Current outputs

* a poorly implement RSS feed of the notifications
* a simple telegram bot notifier

## On Components (notes for Clojure Toronto meetup)

[Watch @stuartsierra video](https://www.youtube.com/watch?v=13cmHf_kt-Q)

He explains all of this way better than I can but I'll leave a quick TL;DR; here

### The problem

* your app has state
  * normally that state is global
  * everything is a sigleton
* a large application is made fs several stateful processes (components)
* the different parts of your application depend on each other

### Component library

#### Components
* gives you an easy way to implement this pattern
* components have a `start`/`stop` lifecycle
* each lifecycle function can return an updated version of the component object
* constructor function is used for configuration that is known a priori
* runtime dependencies are injected (by systems)

#### Systems

* a special component that knows how to start/stop other components
* created with the `system-map` function
* `using` lets you define dependencies between components
* on `start` the library will `assoc` on the component object to inject the dependencies

## The reloaded workflow

[See this blogpost](http://thinkrelevance.com/blog/2013/06/04/clojure-workflow-reloaded)

[reloaded.repl](https://github.com/weavejester/reloaded.repl) provides an implementation allowing us to cut the boilerplate

From the repl

* `(go)` starts the system
* `(reset)` stops, reloads the system and starts it again

## Credits

@mveytsman for showing me the components library
@stuartsierra for creating all this
