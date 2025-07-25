package view

import scala.util.Try

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.DebugTree
import model.errors.DillException
import controller.errors.ErrorController
import controller.tauri.{Tauri, Command}
import controller.viewControllers.{InputViewController, TabViewController, TreeViewController, CodeViewController}


object TabView {
    val inputName: Var[String] = Var("")

    /* Input for the name of the tree to be saved */
    //TODO: use for naming saved trees
    val nameInput: HtmlElement = {
        input(
            typ := "text",
            placeholder := "Enter text...",
            controlled(
                value <-- inputName.signal,
                onInput.mapToValue --> inputName.writer
            )
        )
    }

    /* Renders a tab button from a saved tree name */
    private def tabButton(index: Int): HtmlElement = {
        button(
            className := "tab-button",

            /* Passing on the signal of the selected tab to each tab*/
            cls("selected") <-- TabViewController.tabSelected(index),

            text <-- TabViewController.getFileName(index),
            closeTabButton(index),

            /* Sets selected tab signal to newly selected tab */
            onClick.mapTo(index) --> TabViewController.setSelectedTab
        )
    }

    def closeTabButton(index: Int): HtmlElement = {
        val deleteBus: EventBus[Either[DillException, IndexedSeq[String]]] = EventBus()

        button(
            className := "close-tab-button",

            /* Close 'X' icon */
            i(className := "bi bi-x"),

            /* Deletes the respective tab, not allowing propogation for the tab select onClick */
            onClick.stopPropagation.flatMapTo(TabViewController.deleteSavedTree(index)) --> deleteBus.writer,

            /* Pipe errors */
            deleteBus.stream.collectLeft --> ErrorController.setError,

            /* Update selected tab and tab names */
            deleteBus.stream.collectRight --> TabViewController.setFileNames,

            /* Set new selected tab after a deletion */
            deleteBus.stream.collectRight
                .filter(_.nonEmpty)
                .sample(TabViewController.getSelectedTab)
                .map((currIndex: Int) => if currIndex >= index then 0 max (currIndex - 1) else currIndex) --> TabViewController.setSelectedTab
        )
    }

    def apply(): HtmlElement = {
        div(
            className:= "tab-bar",

            /* Update tree on new tab selected */
            TabViewController.getSelectedTab.changes
                .flatMapSwitch(TabViewController.loadSavedTree)
                .collectLeft --> controller.errors.ErrorController.setError,

            TabViewController.getSelectedTab.changes
                .mapToUnit --> DebugTreeDisplay.resetZoom,

            /* If there are no tabs, unload tree and input from frontend */
            TabViewController.noSavedTrees.changes
                .filter(identity)
                .mapToUnit --> TreeViewController.unloadTree,

            TabViewController.noSavedTrees.changes
                .filter(identity)
                .mapToUnit --> InputViewController.unloadInput,

            TabViewController.noSavedTrees.changes
                .filter(identity)
                .mapToUnit --> CodeViewController.unloadCode,

            /* Renders tabs */
            children <-- TabViewController
                .getFileNames.distinct
                .map(_.indices.map(tabButton(_)))
        )
    }
}
