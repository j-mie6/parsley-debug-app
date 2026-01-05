package view

import scala.util.Random
import scala.collection.mutable

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import model.errors.DillException
import controller.AppStateController
import controller.errors.ErrorController
import controller.viewControllers.CodeViewController
import controller.ToastController
import controller.tauri.{Tauri, Event}
import controller.viewControllers.{MainViewController, TreeViewController, InputViewController, TabViewController, StateManagementViewController}

import model.{CodeFileInformation, DebugTree}
import controller.tauri.Command

object MainView extends DebugViewPage {
    // the stream-based counter is a bit awkward with the increment, plus requires a flatMap to make unique nums
    private object FileCounter {
        private val cs = mutable.Map.empty[String, Int]
        def freshName(name: String) = {
            val n = cs.getOrElseUpdate(name, 0)
            cs(name) = n + 1
            s"$name-$n"
        }
    }

    /* Listen for posted tree */
    val (eitherTreeStream, unlistenTree) = Tauri.listen(Event.TreeReady)
    val treeStream = eitherTreeStream.collectRight
    val (newTreeStream, unlistenNewTree) = Tauri.listen(Event.NewTree)

    val (codeStream, unlistenCode) = Tauri.listen(Event.UploadCodeFile)

    /* Render main viewing page */
    def apply(): HtmlElement = {

        val tabBus: EventBus[Either[DillException, IndexedSeq[String]]] = EventBus()

        super.render(Some(
            div(
                EventStream.fromValue(())
                    .take(1)
                    .flatMapTo(Tauri.invoke(Command.DeleteSavedTrees, ()))
                    --> Observer.empty,

                /* Update DOM theme with theme value */
                AppStateController.isLightMode --> AppStateController.updateDomTheme(),

                /* Update any code view streams */
                codeStream.collectRight.map(Some(_)) --> CodeViewController.setCurrentFile,
                codeStream.collectLeft --> ErrorController.setError,


                /* Update tree and input with TreeReady response */
                treeStream --> TreeViewController.setTree,
                //treeStream --> StateManagementViewController.setCurrTree,
                treeStream.map(_.input) --> InputViewController.setInput,
                treeStream.map(tree => CodeFileInformation(tree.parserInfo)) --> CodeViewController.setFileInformation,

                /* Notify of any errors caught by treeStream */
                eitherTreeStream.collectLeft --> ErrorController.setError,

                /* Save any new trees when received */
                newTreeStream
                    .collectRight
                    .sample(TreeViewController.getSessionName.map(FileCounter.freshName))
                    .flatMapSwitch(TabViewController.saveTree) --> tabBus.writer,

                /* Update file names */
                tabBus.stream.collectRight --> TabViewController.setFileNames,

                /* Pipe errors */
                tabBus.stream.collectLeft --> ErrorController.setError,

                /* Set selected tab to newest tree */
                tabBus.stream.collectRight.map(_.length - 1) --> TabViewController.setSelectedTab,

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
                onUnmountCallback(_ => unlistenNewTree.get),
                onUnmountCallback(_ => unlistenCode.get),
            )
        ))
    }
}
