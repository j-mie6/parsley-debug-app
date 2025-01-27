import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri

import pages.DebugViewPage
import debugger.DebugTreeHandler
import debugger.DebugNode

private val tree: Var[Element] = Var(p("None"))

@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    DebugViewPage.page
)
