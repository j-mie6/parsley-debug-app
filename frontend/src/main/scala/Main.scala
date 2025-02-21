import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalajs.dom
import com.raquo.laminar.api.L.*

import view.MainView

import controller.AppStateController
import controller.tauri.Tauri


@main def app = {
    renderOnDomContentLoaded(
        dom.document.getElementById("app"),
        MainView()

    )
}
