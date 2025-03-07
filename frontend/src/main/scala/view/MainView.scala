package view

import scala.util.Random

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.errors.DillException
import controller.AppStateController
import controller.errors.ErrorController
import controller.ToastController
import controller.tauri.{Tauri, Event}
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController, StateManagementViewController}

object MainView extends DebugViewPage {
    
    /* File counter */
    object Counter {
        private val num: Var[Int] = Var(0)
        val increment: Observer[Unit] = num.updater((x, unit) => x + 1)

        
        /* Generate name: tree-{num} for file */
        def genName: Signal[String] = num.signal.map(numFiles => s"tree-${numFiles}")
    }


    /* Listen for posted tree */
    val (treeStream, unlistenTree) = Tauri.listen(Event.TreeReady)
    val (newTreeStream, unlistenNewTree) = Tauri.listen(Event.NewTree)
    
    /* Render main viewing page */
    def apply(): HtmlElement = {

        val tabBus: EventBus[Either[DillException, List[String]]] = EventBus()

        super.render(Some(
            div(
                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(), 

                
                /* Update tree and input with TreeReady response */
                treeStream.collectRight --> TreeViewController.setTree,
                treeStream.collectRight --> StateManagementViewController.setCurrTree,
                treeStream.collectRight.map(_.input) --> InputViewController.setInput,

                /* Notify of any errors caught by treeStream */
                treeStream.collectLeft --> ErrorController.setError,

                /* Save any new trees when received */
                newTreeStream
                    .collectRight
                    .sample(Counter.genName)
                    .flatMapMerge(TabViewController.saveTree) --> tabBus.writer,

                /* Update file names */
                tabBus.stream.collectRight --> TabViewController.setFileNames,

                /* Pipe errors */
                tabBus.stream.collectLeft --> ErrorController.setError,

                /* Set selected tab to newest tree */
                tabBus.stream.collectRight
                    .map((fileNames: List[String]) => fileNames.length - 1) --> TabViewController.setSelectedTab,

                /* Increment name counter */
                newTreeStream.collectRight --> Counter.increment,

                /* Notify of any errors caught by newTreeStream */
                newTreeStream.collectLeft --> ErrorController.setError,


                /* Load main page */
                child <-- MainViewController.getViewElem,

                /* Displaying Dill Exceptions */
                child.maybe <-- ErrorController.getErrorElem,

                /* Displaying Dill Toasts */
                child.maybe <-- ToastController.getToastElem,


                /* Unlisten to TreeReady event */
                onUnmountCallback(_ => unlistenTree.get),
                onUnmountCallback(_ => unlistenNewTree.get)
            )
        ))
    }
}