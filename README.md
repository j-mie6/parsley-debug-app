# Dill ![Github Workflow Status](https://img.shields.io/github/actions/workflow/status/j-mie6/parsley-debug-app/dill-ci.yml?branch=release) ![Github release](https://img.shields.io/github/v/release/j-mie6/parsley-debug-app) ![Github license](https://img.shields.io/github/license/j-mie6/parsley-debug-app) 


## What is Dill?

Dill (**Debugging Interactively in the Parsley Library**) is a cross-platform, visual, reactive and interactive debugger to be used with the _modern_ parser combinator library for Scala, [`Parsley`](https://github.com/j-mie6/parsley).

## How do I use it?

Dill is distributed as a binary executable and can be installed below according to your machine's operating system:

- [Linux]()
- [MacOS]()
- [Windows]()

### Examples

![Debugging "hi world!"](readme/images/DillHiWorld.png)
![Debugging multiplication](readme/images/DillMultiplication.png)

## Bug Reports

If you encounter a bug when using Dill, try and minimise the example of the parser input that triggers the bug. If possible, make a self contained example: this will help to identify the issue without too much issue.

## How does it work?

- A `RemoteView` backend for `parsley-debug` connectw to a HTTP server that is cross-platform against 9 different Scala targets
- Cross-platform HTTP requests are implemented using the Scala library `sttp`
- The parser sends a HTTP request to a running instance of the debugger
- The debugger can render the resulting parse tree and process it independently
- A renderer (in `Tauri`) handles the debug trees