//> using scala 3.6
//> using repository sonatype-s01:snapshots
//> using dep com.github.j-mie6::parsley::5.0.0-M12
//> using dep com.github.j-mie6::parsley-debug::5.0.0-M12
//> using dep com.github.j-mie6::parsley-debug-remote::0.1-ac6943f-SNAPSHOT

import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import parsley.debug.combinator.*
import parsley.debug.DillRemoteView
import scala.annotation.experimental


@experimental @parsley.debuggable
object Parser

@main def go =
	('h' ~> ("ello" | "i") ~> " world!" ~> eof).attach(DillRemoteView).parse("hello world!")
