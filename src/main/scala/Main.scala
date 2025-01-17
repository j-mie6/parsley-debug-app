import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._

import com.raquo.laminar.api.L._

@js.native
@JSImport("@tauri-apps/api/core", "invoke")
def invoke_me_pls[T](cmd: String): js.Promise[T] = js.native

@main def hello = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  appElement()
)

def appElement(): Div = div(
  h1("Helelo2"),
  button(onClick --> {_ => invoke_me_pls("greet") }, "Click Me")
)