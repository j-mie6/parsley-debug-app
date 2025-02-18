import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalajs.dom
import com.raquo.laminar.api.L.*

import view.MainView

import controller.StateController
import controller.tauri.Tauri


@main def app = {
    dom.document.documentElement.setAttribute("data-theme", if StateController.isLightMode.now() then "light" else "dark")
    renderOnDomContentLoaded(
        dom.document.getElementById("app"),
        MainView()

    )
}
