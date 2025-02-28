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

    /* Export tree button */
    val downloadButton: HtmlElement = button(
        className := "tree-view-download-button",

        /* Save button icon */
        i(className := "bi bi-download", fontSize.px := 35),

        /* Exports current tree */
        onClick(_
            .compose(event => event.sample(TabViewController.getSelectedFileName))
            .flatMapMerge(TreeViewController.downloadTree)
        ) --> Observer.empty,
    )

    /* Import tree button */
    val uploadButton: HtmlElement = button(
        className := "tree-view-upload-button",

        /* Save button icon */
        i(className := "bi bi-upload", fontSize.px := 35),

        /* Exports current tree */
        onClick --> (_ => TreeViewController.importTree("/home/ap3222/Downloads/tree-0.json"))
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

        /* Render skip button */
        child(skipButton) <-- TreeViewController.isDebuggingSession,

        /* Renders the tree */
        child <-- TreeViewController.getTreeElem, 
    )
            
}
