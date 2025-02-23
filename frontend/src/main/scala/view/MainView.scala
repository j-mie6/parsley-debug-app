package view

import scala.util.Random

import org.scalajs.dom
import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import controller.tauri.{Tauri, Event}
import controller.AppStateController
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController}


object MainView extends DebugViewPage {
    
    /* Random number generator */
    private val rand = new Random
    
    /* Generate random name for file */
    def genName: String = s"tree-${rand.nextInt(100)}"

    /* Listen for posted tree */
    val (treeStream, unlistenTree) = Tauri.listen(Event.TreeReady)
    val (newTreeStream, unlistenNewTree) = Tauri.listen(Event.NewTree)
    
    /* Render main viewing page */
    def apply(): HtmlElement = {
        super.render(Some(
            div(
                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(), 

                /* Update tree and input with TreeReady response */
                treeStream.collectRight --> TreeViewController.setTree,
                treeStream.collectRight.map(_.input) --> InputViewController.setInput,

                /* Save any new trees when received */
                newTreeStream.collectRight.mapTo(genName)
                    .tapEach(TabViewController.saveTree)
                    .tapEach(TabViewController.addFileName.onNext)
                    .flatMapSwitch(TabViewController.getFileNameIndex)
                    --> TabViewController.setSelectedTab,

                /* Load main page */
                child <-- MainViewController.getViewElem,

                /* Unlisten to TreeReady event */
                onUnmountCallback(_ => unlistenTree.get),
                onUnmountCallback(_ => unlistenNewTree.get)
            )
        ))
    }
}