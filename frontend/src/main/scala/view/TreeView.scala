package view

import scala.util.Failure
import scala.util.Success

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

import model.DebugTree
import controller.viewControllers.TreeViewController
import controller.viewControllers.TabViewController

/* Object containing rendering functions for the TreeView */
object TreeView {    

    /* Fast forward icon for skipping */
    private lazy val skipIcon: Element = i(className := "bi bi-fast-forward-fill", fontSize.px := 33)

    /* Adds ability to skip the current breakpoint. */
    private lazy val skipButton: Element = button(
        className := "skip-button",

        skipIcon, /* Fast forward icon */

        onClick --> { _ => {
            TreeViewController.skipBreakpoints(0)
        }}
    )


    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(
        /* Render skip button */
        child(skipButton) <-- TreeViewController.isDebuggingSession,

        /* Renders the tree */
        child <-- TreeViewController.getTreeElem, 
    )
            
}
