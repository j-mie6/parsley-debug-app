import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri
import Display.DisplayTree
import Display.DebugTreeHandler
import scala.util.{Try, Success, Failure}

@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    appElement()
)


val textSignal: Var[String] = Var("Nothing")
val treeText: Div = div(text <-- textSignal)

def appElement(): Div = div(
    h1("Dill - Parsley Debugging"),
    treeText,
    button(onClick --> { _ => {
        for {
            text <- Tauri.invoke[String]("tree_text")
        } do {
            DebugTreeHandler.decodeDebugTree(text) match {
                case Success(tree) => textSignal.set(tree.toString())
                case Failure(err) => textSignal.set(err.getMessage())
            }
        }
    }}, "Reload tree"),
)
