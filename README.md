# Dill ![Github Workflow Status](https://img.shields.io/github/actions/workflow/status/j-mie6/parsley-debug-app/dill-ci.yml?branch=release) ![Github release](https://img.shields.io/github/v/release/j-mie6/parsley-debug-app) ![Github license](https://img.shields.io/github/license/j-mie6/parsley-debug-app) 


## What is Dill?

Dill (**Debugging Interactively in the Parsley Library**) is a cross-platform, visual, reactive and interactive debugger to be used with the _modern_ parser combinator library for Scala, [`Parsley`](https://github.com/j-mie6/parsley).

## How do I install it?

Dill is distributed as a binary executable and can be installed below according to your machine's operating system from the [releases](https://github.com/j-mie6/parsley-debug-app/releases/) page.

## Usage

The following example uses `Parsley 5.0.0-M10`:

```scala
import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import parsley.debug.combinator.*
import parsley.debug.DillRemoteView

import parsley.Parsley
import parsley.character.digit
import parsley.syntax.character.charLift
import parsley.expr.{precedence, SOps, InfixL, Atoms}

val number = digit.foldLeft1[Int](0)((n, d) => n * 10 + d.asDigit)

sealed trait Expr
    case class Add(x: Expr, y: Term) extends Expr
    case class Sub(x: Expr, y: Term) extends Expr

sealed trait Term extends Expr
    case class Mul(x: Term, y: Atom) extends Term

sealed trait Atom extends Term
    case class Number(x: Int) extends Atom
    case class Parens(x: Expr) extends Atom

lazy val expr: Parsley[Expr] = precedence {
    Atoms(number.map(Number), '(' ~> expr.map(Parens) <~ ')') :+
    SOps(InfixL)('*' as Mul) :+
    SOps(InfixL)('+' as Add, '-' as Sub)
}

// Parsing "hi world!"
('h' ~> ("ello" | "i") ~> " world!").attach(DillRemoteView).parse("hi world!")

// Parsing a multiplication expression
expr.attach(DillRemoteView).parse("7 + 6 * (5 - 3)")
```

### Examples

The parser inputs given above generate these debug trees:

![Debugging "hi world!"](readme/images/DillHiWorld.png)
![Debugging multiplication](readme/images/DillMultiplication.png)

## Bug Reports

If you encounter a bug when using Dill, try and minimise the example of the parser input that triggers the bug. If possible, make a self contained example: this will help to identify the issue without too much issue.

## How does it work?

- A `RemoteView` backend for `parsley-debug` connects to a HTTP server that is cross-platform against 9 different Scala targets
- Cross-platform HTTP requests are implemented using the Scala library `sttp`
- The parser sends a HTTP request to a running instance of the debugger
- The debugger can render the resulting parse tree and process it independently
- A renderer (in `Tauri`) handles the debug trees