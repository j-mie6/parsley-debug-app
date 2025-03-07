import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import view.MainView

import controller.AppStateController
import controller.tauri.Tauri

@main def app = {
    renderOnDomContentLoaded(
        dom.document.getElementById("app"),
        MainView()
    )
}