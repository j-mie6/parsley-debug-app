package view

import scala.util.Failure
import scala.util.Success

import org.scalajs.dom
import com.raquo.laminar.api.L.*

import model.DebugTree
import controller.viewControllers.TreeViewController
import controller.errors.ErrorController

/**
  * Object containing rendering functions for the TreeView
  */
object TreeView {

    /* Fast forward icon for skipping */
    private lazy val skipIcon: Element = i(className := "bi bi-fast-forward-fill", fontSize.px := 33)

    /* Adds ability to skip the current breakpoint. */
    private lazy val skipButton: Element = button(
        className := "skip-button",

        skipIcon, /* Fast forward icon */

        onClick(_
            .sample(TreeViewController.getSessionId)
            .flatMapMerge(TreeViewController.skipBreakpoints(_, 0))
            .collectLeft
        ) --> ErrorController.setError,
    )


    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(
        child(skipButton) <-- TreeViewController.isDebuggingSession,
        child <-- TreeViewController.getTreeElem, /* Renders the tree */
    )
            
}
