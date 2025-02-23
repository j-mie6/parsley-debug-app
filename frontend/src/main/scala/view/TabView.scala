package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.tauri.Tauri
import controller.viewControllers.TabViewController
import controller.viewControllers.TreeViewController
import controller.tauri.Command


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
            transition := "all 0.5s", //TODO: move to css

            text <-- TabViewController.getFileName(index),
            closeTabButton(index),

            /* Sets selected tab signal to newly selected tab */
            onClick.mapTo(index) --> TabViewController.setSelectedTab //FIXME
        )
    }

    def closeTabButton(index: Int): HtmlElement = {
        button(
            className := "close-tab-button",

            /* Close 'X' icon */
            i(className := "bi bi-x"),

            /* Deletes the respective tab */
            onClick(event => event.sample(TabViewController.getFileName(index))
                .compose(TabViewController.deleteSavedTree)
            ) --> TabViewController.setFileNames,
        )
    }
    
    def apply(): HtmlElement = {
        div(
            className:= "tab-bar",

            //TODO: neaten and move to TreeViewController
            TabViewController.getSelectedTab.changes
                .flatMapSwitch(TabViewController.getFileName)
                .compose(TabViewController.loadSavedTree)
                --> TreeViewController.setTree,

            /* Renders tabs */ 
            children <-- TabViewController.getFileNames.signal.map(
                _.indices.map(tabButton(_))
            )
        )
    }
}