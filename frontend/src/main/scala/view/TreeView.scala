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

    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(

        /* Render save button */
        child(downloadButton) <-- TreeViewController.treeExists,

        /* Renders the tree */
        child <-- TreeViewController.getTreeElem,
    )
            
}
