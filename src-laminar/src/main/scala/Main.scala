import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

import Display.DisplayTree

import com.raquo.laminar.api.L._

import lib.Tauri

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
