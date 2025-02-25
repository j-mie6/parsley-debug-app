# Dill ![Github Workflow Status](https://img.shields.io/github/actions/workflow/status/j-mie6/parsley-debug-app/dill-ci.yml?branch=main) ![Github release](https://img.shields.io/github/v/release/j-mie6/parsley-debug-app) ![Github license](https://img.shields.io/github/license/j-mie6/parsley-debug-app) 


## What is Dill?

`Dill` (**Debugging Interactively in the Parsley Library**) is a cross-platform, visual, interactive debugger to be used with the parser combinator library for Scala, [`Parsley`](https://github.com/j-mie6/parsley).


## How do I install it?

`Dill` is distributed as a binary executable and can be installed below according to your machine's operating system from the [releases](https://github.com/j-mie6/parsley-debug-app/releases/) page.
To build the `Dill` debugger on your own machine, go to [building](#building).


## Usage

You will first need to [install](#how-do-i-install-it) / [build](#building) the `Dill` debugging application onto your machine. Then once the application has started, you are ready to start sending it debug information from within `Parsley`:

- First, ensure that your project has the `remote-view` project as a dependency (you will of course need to have the `Parsley` library as a dependency too).
- Import the `DillRemoteView` object from `parsley.debug`.
- Then on the parser which you would like to debug, attach the `DillRemoteView` object, before the parse step.

**`test.sc`**: the following `scala` script uses `Parsley 5.0.0-M12`

```scala

//> using repository sonatype-s01:snapshots

//> using dep com.github.j-mie6::parsley:5.0.0-M12
//> using dep com.github.j-mie6::parsley-debug:5.0.0-M12 
//> using dep com.github.j-mie6::parsley-debug-remote::0.1-ac6943f-SNAPSHOT
//> using options -experimental

import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import parsley.debug.combinator.*
import parsley.debug.DillRemoteView
import scala.annotation.experimental

/* Annotations to assign parser names to combinators */ 
@experimental @parsley.debuggable 
object Parser {
    val hello: Parsley[Unit] = whitespaces ~> ('h' ~> ("ello" | "i")).void <~ whitespaces
    val subject: Parsley[Unit] = whitespaces ~> ("world" | "jamie").void <~ whitespaces
    val greeting: Parsley[Unit] = (hello ~> subject ~> "!" ~> eof)
}

Parser.greeting.attach(DillRemoteView).parse("hello jamie!")

```

_To run this snippet, simply run_ `scala test.sc`.

You will then be able to view a representation of the abstract syntax tree generated by `Parsley`:

![Debugging "hello jamie!"](readme/images/DillHelloJamie.png)


## Building

The frontend of the application is written using [`ScalaJS`](https://www.scala-js.org/) and [`Laminar`](https://laminar.dev/), and uses the `sbt` build system, the frontend compiles down to a single `JavaScript` file located in `./static`. The backend uses the [`Tauri`](https://v2.tauri.app/) package to host the frontend, and the [`Rocket`](https://rocket.rs/) package to host a server to receive the tree from `Parsley`. We use `npm` to manage the various packages.

**To run the project, execute `sbt run`:**

This will install the node packages required to build the project, build the front and backend, and then start the generated executable.

**To run the project in development mode, execute:**
- `sbt ~buildFrontend` to start the sbt frontend development server.
- `sbt runBackend` in a different terminal to start the [`Tauri`](https://v2.tauri.app/) app.

_This will cause a quick-reload when any of the source files are modified._


## Bug Reports

If you encounter a bug when using `Dill`, please try to make a self contained example: this will help to identify the issue.
Then, create a new [issue](https://github.com/j-mie6/parsley-debug-app/issues) - if possible, please include screenshots and a description of the bug.


## How does it work?

- The `remote-view` backend for `parsley-debug` posts the debug tree from the parser to the [`Rocket`](https://rocket.rs/) HTTP server running within the `Dill` debugger.
- The [`Rocket`](https://rocket.rs/) server transforms and passes off a representation of the debug tree to the [`Tauri`](https://v2.tauri.app/) application in `Rust`.
- The frontend then queries the [`Tauri`](https://v2.tauri.app/) application for the debug tree.
- Upon receiving of the tree, the frontend renders the tree on the screen.


## Docker

We provide a Dockerfile that lets you modify and test the project without installing additional dependencies.

### How to use?
After building an image, you can start a container using the command `docker run -it --rm -e DISPLAY=:0 -v /tmp/.X11-unix:/tmp/.X11-unix -p 2222:22 <IMAGE_ID>`. **You must be using a Linux-based terminal or have an X-Server installed on your machine to support GUI forwarding.**

This will launch the container with SSH access and X-Forwarding enabled, allowing the GUI to display on your local machine. Once the container is running, you can SSH into it using `ssh -X -p 2222 root@localhost`, then navigate to the Dill root directory with `cd /home`.

Before running the project, ensure that Rust's package manager is set to the latest version by running `rustup default stable`. Once this is done, you can follow the commands listed in [building](#building) to work on Dill.

### Editing Outside Docker
Alternatively, if you prefer working on the code outside the container, you can edit files locally and copy them into the container using: `sbt dockerBuild`.