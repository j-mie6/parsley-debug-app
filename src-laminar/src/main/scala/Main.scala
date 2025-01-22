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
val tree_text: Div = div(text <-- textSignal)

def appElement(): Div = div(
    h1("Dill - Parsley Debugging"),
    tree_text,
    button(onClick --> { _ => {
        for {
            text <- Tauri.invoke[String]("tree_text")
        } do textSignal.set(text)
    }}, "Reload tree"),
)
