package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.MainViewController
import controller.tauri.{Tauri, Event}
import controller.TreeViewController


object MainView extends DebugViewPage {
    def apply(): HtmlElement = {

        Tauri.listen[Unit](Event.TreeReady, {_ => TreeViewController.reloadTree()})

        super.render(Some(div(
                    child <-- MainViewController.getMainView
            ))
        )
    }
}