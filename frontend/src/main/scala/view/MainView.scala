package view

import scala.util.Random

import org.scalajs.dom
import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import controller.tauri.{Tauri, Event}
import controller.errors.ErrorController
import controller.AppStateController
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController}
import model.errors.DillException

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

        /* if debugging you have sessionID
           if you have session id of -1, do normal stuff
           else you have some session id: (if you have session id it SHOULD be current tab (getSelectedTab))
                search for the tree to update with sessionid (map in backend of running debugging sessions (int) to name)
                    with the search, if not found its a new session, make tab etc
                update tree (Set tree) (new function, takes index and current state then updates saved tree with current loaded tree)
                update input
         */
        super.render(Some(
            div(
                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(), 

                
                /* Update tree and input with TreeReady response */
                treeStream.collectRight --> TreeViewController.setTree,
                treeStream.collectRight.map(_.input) --> InputViewController.setInput,

                /* Notify of any errors caught by treeStream */
                treeStream.collectLeft --> ErrorController.setError,

                /* START */

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

                /* END */

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