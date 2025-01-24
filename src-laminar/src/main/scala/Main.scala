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
    DebugViewPage.page
)
