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
    private def tabButton(tabName: String): HtmlElement = {
        button(
            className := "tab-button",

            /* Passing on the signal of the selected tab to each tab*/
            cls("selected") <-- TabViewController.isSelectedTab(tabName),
            transition := "all 0.5s", //TODO: move to css

            tabName,
            closeTabButton(tabName),

            /* Sets selected tab signal to newly selected tab */
            onClick(_ => TabViewController.getTab(tabName)) --> TabViewController.setSelectedTab()
        )
    }

    def closeTabButton(tabTitle: String): HtmlElement = {
        button(
            className := "close-tab-button",

            /* Close 'X' icon */
            i(className := "bi bi-x"),

            /* Deletes the respective tab */
            onClick(_ => TabViewController.deleteTab(tabTitle)) --> TabViewController.setFileNames(),
        )
    }
    
    def apply(): HtmlElement = {
        div(
            className:= "tab-bar",

            //TODO: neaten and move to TreeViewController
            TabViewController.getSelectedTab.changes
                .compose(TabViewController.loadSavedTree) 
                --> TreeViewController.setTree(),

            /* Renders tabs */ 
            children <-- TabViewController.getFileNames.signal.map(
                (names: List[String]) => names.map(tabButton(_))
            )
        )
    }
}