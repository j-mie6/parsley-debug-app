//> using repository sonatype-s01:snapshots

//> using dep com.github.j-mie6::parsley:5.0.0-M14
//> using dep com.github.j-mie6::parsley-debug:5.0.0-M14
//> using dep com.github.j-mie6::parsley-debug-remote::0.1-5c47cfb-SNAPSHOT
//> using options -experimental

import parsley.debug.*
import parsley.debug.combinator.*
import parsley.state.*
import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import scala.annotation.experimental

implicit class RefExtensions[B](r: Ref[B]) {
  implicit def encodable(implicit c: Codec[B]): RefCodec = new RefCodec {
        type A = B
        val ref: Ref[A] = r
        val codec: Codec[A] = c
  }
}


@experimental @parsley.debuggable
object Parser {

    val openTag = (atomic('<' <* notFollowedBy('/')))

    val openHeader = openTag *> "h1" <* '>'
    val openUnderline = openTag *> 'u' <* '>'

    val html = openHeader.fillRef { r1 => 
        openUnderline.fillRef { r2 => 
            stringOfSome(letter).break(ExitBreak, r1.encoded, r2.encoded) <* ("</" *> r2.get.flatMap(char) <* ">")
        } <* ("</" *> r1.get.flatMap(string) <* ">")
    }
}

Parser.html.attach(DillRemoteView).parse("<h1><u>Hello</u></demo>") 

