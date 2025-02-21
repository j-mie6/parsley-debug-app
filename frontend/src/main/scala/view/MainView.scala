package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.tauri.{Tauri, Event}
import controller.viewControllers.MainViewController
import controller.viewControllers.TreeViewController


object MainView extends DebugViewPage {
    def apply(): HtmlElement = {

        val (stream, unlisten) = Tauri.listen(Event.TreeReady)

        super.render(Some(
            div(
                stream.collectRight --> TreeViewController.setTree,
                child <-- MainViewController.getMainView,
                onUnmountCallback(_ => unlisten.get)
            )
        ))
    }
}