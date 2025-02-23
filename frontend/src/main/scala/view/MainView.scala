package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.tauri.{Tauri, Event}
import controller.AppStateController
import controller.viewControllers.{MainViewController, TreeViewController}


object MainView extends DebugViewPage {
    def apply(): HtmlElement = {
        val (stream, unlisten) = Tauri.listen(Event.TreeReady)

        super.render(Some(
            div(
                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(), 

                /* Update tree with TreeReady response */
                stream.collectRight --> TreeViewController.setTree,

                /* Load main page */
                child <-- MainViewController.getViewElem,

                /* Unlisten to TreeReady event */
                onUnmountCallback(_ => unlisten.get)
            )
        ))
    }
}