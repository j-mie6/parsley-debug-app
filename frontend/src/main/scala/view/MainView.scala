package view

import scala.util.Random

import org.scalajs.dom
import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import controller.tauri.{Tauri, Event}
import controller.errors.ErrorController
import controller.AppStateController
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController}
import controller.viewControllers.CodeViewController

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

    val (fileNameStream, unlistenFileName) = Tauri.listen(Event.UploadFiles)
    val (codeStream, unlistenCode) = Tauri.listen(Event.UploadCodeFile)
    
    /* Render main viewing page */
    def apply(): HtmlElement = {
        super.render(Some(
            div(
                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(), 

                /* Update any code view streams */
                codeStream.collectRight.map(Some(_)) --> CodeViewController.setCurrentFile,
                fileNameStream.collectRight.map(Some(_)) --> CodeViewController.setFileInformation,
                
                codeStream.collectLeft --> ErrorController.setError,
                fileNameStream.collectLeft --> ErrorController.setError,


                /* Update tree and input with TreeReady response */
                treeStream.collectRight --> TreeViewController.setTree,
                treeStream.collectRight.map(_.input) --> InputViewController.setInput,

                /* Notify of any errors caught by treeStream */
                treeStream.collectLeft --> ErrorController.setError,

                
                /* Save any new trees when received */
                newTreeStream.collectRight.sample(Counter.genName)
                    .flatMapMerge(TabViewController.saveTree)
                    .collectLeft --> ErrorController.setError,

                /* Add new tab when new tree saved */ 
                newTreeStream.collectRight
                    .sample(Counter.genName) --> TabViewController.addFileName,

                /* Update tab index */
                newTreeStream.collectRight
                    .sample(Counter.genName)
                    .flatMapSwitch(TabViewController.getFileNameIndex)
                    --> TabViewController.setSelectedTab,
                
                /* Increment name counter */
                newTreeStream.collectRight --> Counter.increment,

                /* Notify of any errors caught by newTreeStream */
                newTreeStream.collectLeft --> ErrorController.setError,


                /* Load main page */
                child <-- MainViewController.getViewElem,

                /* Displaying Dill Exceptions */
                child.maybe <-- ErrorController.getErrorElem,


                /* Unlisten to TreeReady event */
                onUnmountCallback(_ => unlistenTree.get),
                onUnmountCallback(_ => unlistenNewTree.get),
                onUnmountCallback(_ => unlistenCode.get),
                onUnmountCallback(_ => unlistenFileName.get),
            )
        ))
    }
}