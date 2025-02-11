package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.DebugTreeHandler
import controller.TabController
import controller.Tauri
import controller.TreeController

/**
  * Object containing rendering functions for the TreeViewPage.
  */
object TreeViewPage extends DebugViewPage {
    /* Default tree view when no tree is loaded */
    private val noTreeFound: HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )

    /* Visual call to action  for user to save the current tree */
    private lazy val saveIcon: HtmlElement = i(className := "bi bi-floppy-fill",
        fontSize.px := 25, marginRight.px := 10)

    private val rand = new scala.util.Random /* Random number generator */

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: Element = button(
        className := "tree-view-save",

        saveIcon, /* Floppy disk icon */

        onClick --> { _ => {
            /* DO SAVE POP UP LOGIC HERE */

            TabController.saveTree((rand.nextInt).toString())
        }}
    )

    def apply(): HtmlElement = {
        TreeController.setDisplayTree(noTreeFound)

        Tauri.listen[Unit]("tree-ready", {_ => TreeController.reloadTree()})
        
        super.render(Some(div(
            className := "tree-view-page",
            saveButton,            
            child <-- TreeController.getDisplayTree, /* Renders tree */
        )))
    }
}
