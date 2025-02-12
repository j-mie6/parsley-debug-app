package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import controller.TabController
import controller.Tauri
import controller.TreeController


object ScrollableTabView {
    val inputName: Var[String] = Var("")

    /* Input for the name of the tree to be saved */
    val nameInput =
        input(
            typ := "text",
            placeholder := "Enter text...",
            controlled(
                value <-- inputName.signal,
                onInput.mapToValue --> inputName.writer
            )
        )

    /* Renders a tab button from a saved tree name */
    private def tabButton(tabTitle: String, selectedTab: Var[String]): HtmlElement = {
            button(
                className := "tab-button",
                /* Passing on the signal of the selected tab to each tab*/
                className <-- TabController.isSelectedTab(tabTitle).map(selected => {
                        if selected then
                            "selected"
                        else
                            ""
                }),
                transition := "all 0.5s",
                tabTitle,
                button(
                    className := "close-tab-button",
                    borderColor := "transparent",
                    backgroundColor := "transparent",
                    transition := "all 0.5s",
                    i(className := "bi bi-x-circle"),

                    onClick --> {_ => {
                            /* Deletes the respective tab */
                            TabController.deleteTab(tabTitle)

                            /* Reloads the tree if this was the selected tab */
                            TabController.isSelectedTab(tabTitle).map(selected => {
                                if selected then
                                    TreeController.reloadTree()
                            })
                        }
                    }
                ),

                onClick --> {_ => {
                        /* Sets selected tab signal to newly selected tab */
                        TabController.setSelectedTab(tabTitle)

                        /* Loads in the respective tree */
                        TabController.loadSavedTree(tabTitle)
                    }
                }
            )
    }
    
    def apply(): HtmlElement = {
        div(
            className:= "scrollable-tab-bar",

            /* Renders tabs */ 
            children <-- TabController.getFileNames.signal.map(names =>
                names.map(name => {
                    tabButton(name, TabController.getSelectedTab)
                })
            )
        )
    }
}