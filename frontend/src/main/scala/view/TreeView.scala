package view

import scala.util.Failure
import scala.util.Success

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.features.unitArrows

import model.DebugTree
import controller.viewControllers.TreeViewController

/* Object containing rendering functions for the TreeView */
object TreeView {

    val downloadButton: HtmlElement = button(
        className := "tree-view-download-button",

        /* Save button icon */
        i(className := "bi bi-download", fontSize.px := 35),

        /* Deletes the respective tab */
        onClick --> println("Stop tapping me!!!!"),
    )

    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(

        /* Render save button */
        child(downloadButton) <-- TreeViewController.treeExists,

        /* Renders the tree */
        child <-- TreeViewController.getTreeElem,
    )
            
}
