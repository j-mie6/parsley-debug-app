import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri
import Display.DisplayTree


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
            text <- Tauri.invoke[String]("render_debug_tree")
        } do textSignal.set(text)
    }}, "Reload tree"),
)
