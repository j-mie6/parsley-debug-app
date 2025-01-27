import scala.util.{Try, Success, Failure}

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import lib.Tauri

import pages.TreeViewPage

@main def app = {
    val darkPreferred : Boolean = dom.window.matchMedia("(prefers-color-scheme: dark)").matches
    dom.document.documentElement.setAttribute("data-theme", if darkPreferred then "dark" else "light")
    renderOnDomContentLoaded(
        dom.document.getElementById("app"),
        TreeViewPage()
    )
}
