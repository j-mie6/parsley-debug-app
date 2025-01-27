import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri

// import pages.DebugViewPage
import pages.BasePage
import pages.TreeView
import pages.Page
import debugger.DebugTreeHandler
import debugger.DebugNode

def funct() = {
    dom.window.localStorage.setItem("", "1")
}

@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    TreeView().render()
)
