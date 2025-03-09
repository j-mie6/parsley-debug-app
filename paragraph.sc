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
    
    val word = many(letter)

    val sentence = sepBy(word, ' ') <~ '.'

    val paragraph = many(sentence) <* eof
}

val s: String = """Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt
    ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut
    aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore
    eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
    mollit anim id est laborum.""".stripMargin.replaceAll("\n", " ")

Parser.paragraph.attach(DillRemoteView).parse(s)