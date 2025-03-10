//> using repository sonatype-s01:snapshots

//> using dep com.github.j-mie6::parsley:5.0.0-M14
//> using dep com.github.j-mie6::parsley-debug:5.0.0-M14
//> using dep com.github.j-mie6::parsley-debug-remote::0.1-5c47cfb-SNAPSHOT
//> using options -experimental

import parsley.debug.*
import parsley.debug.combinator.*
import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import scala.annotation.experimental

@experimental @parsley.debuggable
object Parser {

    val word = many(letter <|> digit <|> '+')

    val param = (word <* '=') <~> word

    val queryParams = '?' *> sepBy(param, '&') <* eof
}

Parser.queryParams.attach(DillRemoteView).parse("?id=50&name=ferret&color=deep+purple")
