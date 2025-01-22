import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri
import pages.DebugViewPage
import Display.DisplayTree


@main def app = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    // DebugViewPage()
    DebugViewPage.page
)
