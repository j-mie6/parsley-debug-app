package view

import scala.util.Failure
import scala.util.Success

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.DebugTree
import controller.viewControllers.TreeViewController
import controller.viewControllers.TabViewController
import controller.errors.ErrorController
import controller.viewControllers.StateManagementViewController
import model.errors.DillException

/**
  * Object containing rendering functions for the TreeView
  */
object TreeView {

    /* Render tree as HtmlElement */
    def apply(): HtmlElement = {
        val returnBus: EventBus[Either[DillException, Seq[(Int, String)]]] = EventBus()

        val seqBus: EventBus[Seq[(Int, String)]] = EventBus()
        val debuggableBus: EventBus[Boolean] = EventBus()

        div(
            TreeViewController.isDebuggingSession.changes
                .filterNot(identity)
                .mapToUnit --> StateManagementViewController.clearRefs,

            TreeViewController.getSessionId.changes.flatMapSwitch(TreeViewController.getRefs) --> returnBus.writer,

            returnBus.stream.collectRight --> seqBus.writer,
            returnBus.stream.collectLeft --> ErrorController.setError,

            seqBus.stream.sample(TreeViewController.isDebuggingSession) --> debuggableBus.writer,

            debuggableBus.stream.filter(identity).flatMapTo(seqBus.stream) --> StateManagementViewController.setRefs,
            debuggableBus.stream.filterNot(identity).mapTo(Nil) --> StateManagementViewController.setRefs,

            child <-- TreeViewController.getTreeElem, /* Renders the tree */
        )
    }

}
