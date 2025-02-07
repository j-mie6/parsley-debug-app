package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.DebugTreeHandler
import controller.TreeController
import controller.Tauri

/**
  * Object containing rendering functions for the TreeViewPage.
  */
object TreeViewPage extends DebugViewPage {
    // The HTML element of the tree to display
    private val displayTree: Var[HtmlElement] = Var(DebugTreeDisplay(DebugTree.Sample))

    /**
      * Render the TreeViewPage.
      *
      * @return The HTML element representing the debug tree.
      */
    def apply(): HtmlElement = {
        Tauri.listen[Unit]("tree-ready", {_ => TreeController.reloadTree(displayTree)})
        super.render(Some(div(
            className := "tree-view-page",

            child <-- displayTree
        )))
    }
}
