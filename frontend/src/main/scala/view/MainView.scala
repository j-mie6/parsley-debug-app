package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.MainViewHandler
import controller.tauri.{Tauri, Event}
import controller.TreeController


object MainView extends DebugViewPage {
    def apply(): HtmlElement = {

        Tauri.listen[Unit](Event.TreeReady, {_ => TreeController.reloadTree()})

        super.render(Some(div(
                    child <-- MainViewHandler.getMainView
            ))
        )
    }
}