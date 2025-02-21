package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.tauri.{Tauri, Event}
import controller.errors.ErrorController
import controller.viewControllers.MainViewController
import controller.viewControllers.TreeViewController

object MainView extends DebugViewPage {

    val root: HtmlElement = div(
        MainView(),
        ErrorController.displayError,
    )

    def apply(): HtmlElement = {
        Tauri.listen[Unit](Event.TreeReady, {_ => TreeViewController.reloadTree()})

        super.render(Some(div(
                    child <-- MainViewController.getMainView
            ))
        )
    }
}