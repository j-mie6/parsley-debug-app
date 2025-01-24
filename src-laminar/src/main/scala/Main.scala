import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri
import displays.DisplayTree
import debugger.DebugTreeHandler
import debugger.DebugNode
import scala.util.{Try, Success, Failure}

@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    appElement()
)

val textVar: Var[String] = Var("")
val treeText: Div = div(text <-- textVar)

def appElement(): Div = div(
    h1("Dill - Parsley Debugging"),
    treeText,
    button(onClick --> { _ => {
        for {
            text <- Tauri.invoke[String]("fetch_debug_tree")
        } do {
            DebugTreeHandler.decodeDebugTree(text) match {
                case Success(tree) => textVar.set(tree.toString())
                case Failure(err) => textVar.set(err.getMessage())
            }
        }
    }}, "Reload tree"),
)
