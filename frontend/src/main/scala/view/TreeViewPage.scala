package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.DebugTreeHandler
import controller.TabController
import controller.TreeController
import controller.tauri.{Tauri, Event}
import controller.tauri.Event.eventToString

/**
  * Object containing rendering functions for the TreeViewPage.
  */
object TreeViewPage extends DebugViewPage {

    /* Visual call to action  for user to save the current tree */
    private lazy val saveIcon: HtmlElement = i(className := "bi bi-floppy-fill",
        fontSize.px := 25, marginRight.px := 10)

    private val rand = new scala.util.Random /* Random number generator */

    /* Adds ability to save and store current tree. */
    private lazy val saveButton: Element = button(
        className := "tree-view-save",

        saveIcon, /* Floppy disk icon */

        onClick --> { _ => {
            /* TODO: Save popup logic */
            val treeName: String = (rand.nextInt).toString()
            TabController.saveTree(treeName)
            TabController.setSelectedTab(treeName)

        }}
    )

    def apply(): HtmlElement = {        
        super.render(Some(div(
            className := "tree-view-page",
            saveButton,
            TreeController.getDisplayTree, /* Renders tree */
        )))
    }
}
