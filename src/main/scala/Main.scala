import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L._
import lib.Tauri

@main def hello = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  appElement()
)

val textSignal: Var[String] = Var("Nothing")
val myDiv: Div = div(text <-- textSignal)

def appElement(): Div = div(
  h1("Hey!"),
  button(onClick --> {_ => {
    for {
      text <- Tauri.invoke[String]("text")
    } do textSignal.set(text)
  } }, "Click Me"),
  myDiv
)