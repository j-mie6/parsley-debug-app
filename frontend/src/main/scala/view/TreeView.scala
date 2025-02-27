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

    /* Gets name of current tree and passes it to downloadTree */
    def downloadCurrentTree: Unit = {
        TabViewController.getSelectedFileName.changes.recoverToTry.collectSuccess
            .flatMapMerge(TreeViewController.downloadTree) --> Observer.empty
    }

    /* Export tree button */
    val downloadButton: HtmlElement = button(
        className := "tree-view-download-button",

        /* Save button icon */
        i(className := "bi bi-download", fontSize.px := 35),

        /* Exports current tree */
        onClick --> downloadCurrentTree,
    )

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
        /* Render download button */
        child(downloadButton) <-- TreeViewController.treeExists,
        child(skipButton) <-- TreeViewController.isDebuggingSession,
        child <-- TreeViewController.getTreeElem, /* Renders the tree */
    )
            
}
