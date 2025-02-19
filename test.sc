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

@experimental @parsley.debuggable
object Parser {
    import parsley.character.digit
    import parsley.expr.{InfixL, Ops, precedence}
    val natural: Parsley[Int] = digit.foldLeft1(0)((n, d) => n * 10 + d.asDigit)
    val hello: Parsley[Unit] = ('h' ~> ("ello" | "i") ~> " world!").void

    val int: Parsley[BigInt] = satisfy(_.isDigit).foldLeft1(BigInt(0))((acc, c) => acc * 10 + c.asDigit)

    lazy val expr: Parsley[BigInt] = precedence[BigInt](int, char('(') ~> expr <~ char(')'))(
        Ops(InfixL)(char('*') as (_ * _), char('/') as (_ / _)),
        Ops(InfixL)(char('+') as (_ + _), char('-') as (_ - _))
    )

    lazy val prog: Parsley[List[BigInt]] = many(many(endOfLine) ~> expr)
}

Parser.prog.attach(DillRemoteView).parse("(1+2)*(4-3)")