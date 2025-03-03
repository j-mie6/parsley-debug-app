//> using repository sonatype-s01:snapshots

//> using dep com.github.j-mie6::parsley:5.0.0-M12
//> using dep com.github.j-mie6::parsley-debug:5.0.0-M12 
//> using dep com.github.j-mie6::parsley-debug-remote::0.1-6e983ff-SNAPSHOT
//> using options -experimental
import java.beans.Expression
import parsley.Parsley
import parsley.generic
import parsley.quick.*
import parsley.syntax.character.{charLift, stringLift}
import parsley.debug.combinator.*
import parsley.debug.DillRemoteView
import scala.annotation.experimental

trait ParserSingletonBridgePos[+A] extends generic.ErrorBridge {
    protected def con(pos: (Int, Int)): A
    def from(op: Parsley[_]): Parsley[A] = error(pos.map(this.con(_)) <* op)
    final def <#(op: Parsley[_]): Parsley[A] = this from op
}

trait ParserBridgePos1[-A, +B] extends ParserSingletonBridgePos[A => B] {
    def apply(x: A)(pos: (Int, Int)): B
    def apply(x: Parsley[A]): Parsley[B] = error(ap1(pos.map(con), x))

    override final def con(pos: (Int, Int)): A => B = this.apply(_)(pos)
}

trait ParserBridgePos2[-A, -B, +C] extends ParserSingletonBridgePos[(A, B) => C] {
    def apply(x: A, y: B)(pos: (Int, Int)): C
    def apply(x: Parsley[A], y: =>Parsley[B]): Parsley[C] = error(ap2(pos.map(con), x, y))

    override final def con(pos: (Int, Int)): (A, B) => C = this.apply(_, _)(pos)
}

object lexer {
    import parsley.token.{Lexer, Basic}
    import parsley.token.descriptions.{LexicalDesc, NameDesc, SymbolDesc}

    private val desc = LexicalDesc.plain.copy(
        nameDesc = NameDesc.plain.copy(
            identifierStart = Basic(_.isLetter),
            identifierLetter = Basic(_.isLetter),
        ),
        symbolDesc = SymbolDesc.plain.copy(
            hardKeywords = Set("negate", "let", "in"),
            hardOperators = Set("*", "+", "-"),
        ),
    )

    private val lexer = new Lexer(desc)

    val identifier = lexer.lexeme.names.identifier
    val number = lexer.lexeme.natural.decimal

    def fully[A](p: Parsley[A]) = lexer.fully(p)
    val implicits = lexer.lexeme.symbol.implicits
}

object ast {
    sealed trait LetExpr
    case class Let(bindings: List[Binding], x: Expr)(val pos: (Int, Int)) extends LetExpr
    case class Binding(v: String, x: LetExpr)(val pos: (Int, Int))

    sealed trait Expr extends LetExpr
    case class Add(x: Expr, y: Expr)(val pos: (Int, Int)) extends Expr
    case class Mul(x: Expr, y: Expr)(val pos: (Int, Int)) extends Expr
    case class Sub(x: Expr, y: Expr)(val pos: (Int, Int)) extends Expr
    case class Neg(x: Expr)(val pos: (Int, Int)) extends Expr
    case class Num(x: BigInt)(val pos: (Int, Int)) extends Expr
    case class Var(x: String)(val pos: (Int, Int)) extends Expr

    object Let extends ParserBridgePos2[List[Binding], Expr, LetExpr]
    object Binding extends ParserBridgePos2[String, LetExpr, Binding]
    object Add extends ParserBridgePos2[Expr, Expr, Add]
    object Mul extends ParserBridgePos2[Expr, Expr, Mul]
    object Sub extends ParserBridgePos2[Expr, Expr, Sub]
    object Neg extends ParserBridgePos1[Expr, Neg]
    object Num extends ParserBridgePos1[BigInt, Num]
    object Var extends ParserBridgePos1[String, Var]
}

object expressions {
    import parsley.expr.{precedence, Ops, InfixL, Prefix}
    import parsley.combinator.sepEndBy1

    import lexer.implicits.implicitSymbol
    import lexer.{number, fully, identifier}
    import ast._

    private lazy val atom: Parsley[Expr] =
        "(" ~> expr <~ ")" | Num(number) | Var(identifier)
    private lazy val expr = precedence[Expr](atom)(
        Ops(Prefix)(Neg from "negate"),
        Ops(InfixL)(Mul from "*"),
        Ops(InfixL)(Add from "+", Sub from "-"))

    private lazy val binding = Binding(identifier, "=" ~> letExpr)
    private lazy val bindings = sepEndBy1(binding, ";")
    private lazy val letExpr: Parsley[LetExpr] =
      Let("let" ~> bindings, "in" ~> expr) | expr

    val parser = fully(letExpr)
}


expressions.parser.attach(DillRemoteView).parse("let x = 10;y = let z = x + 4 in z * z;in x * y")