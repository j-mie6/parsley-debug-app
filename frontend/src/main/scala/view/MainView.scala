package view

import scala.util.Random

import org.scalajs.dom
import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import controller.tauri.{Tauri, Event}
import controller.errors.ErrorController
import controller.AppStateController
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController}

object MainView extends DebugViewPage {
    
    /* File counter */
    object Counter {
        private val num: Var[Int] = Var(0)
        val increment: Observer[Unit] = num.updater((x, unit) => x + 1)

        /* Generate random name for file */
        def genName: Signal[String] = num.signal.map(numFiles => s"tree-${numFiles}")
    }


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

                /* Notify of any errors caught by treeStream */
                treeStream.collectLeft --> ErrorController.setError,

                /* Save any new trees when received */
                newTreeStream.collectRight.sample(Counter.genName).flatMapSwitch(TabViewController.saveTree).collectLeft --> ErrorController.setError,
                newTreeStream.collectRight.sample(Counter.genName)
                    .tapEach(TabViewController.addFileName.onNext)
                    .tapEach(_ => Counter.increment.onNext(()))
                    .flatMapSwitch(TabViewController.getFileNameIndex)
                    --> TabViewController.setSelectedTab,

                /* Notify of any errors caught by newTreeStream */
                newTreeStream.collectLeft --> ErrorController.setError,

                /* Load main page */
                child <-- MainViewController.getViewElem,

                /* Displaying Dill Exceptions */
                child.maybe <-- ErrorController.getErrorElem,

                /* Unlisten to TreeReady event */
                onUnmountCallback(_ => unlistenTree.get),
                onUnmountCallback(_ => unlistenNewTree.get)
            )
        ))
    }
}