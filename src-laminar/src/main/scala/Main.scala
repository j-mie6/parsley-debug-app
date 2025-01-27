import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.State
import lib.Tauri

import pages.TreeViewPage

@main def app = {
    dom.document.documentElement.setAttribute("data-theme", if State.isLightMode.now() then "light" else "dark")
    renderOnDomContentLoaded(
        dom.document.getElementById("app"),
        TreeViewPage()
    )
}
