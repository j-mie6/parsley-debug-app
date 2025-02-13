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
                cls("selected") <-- TabController.isSelectedTab(tabTitle),
                transition := "all 0.5s",
                tabTitle,
                button(
                    className := "close-tab-button",
                    i(className := "bi bi-x"),

                    onClick --> {_ => 
                        /* Deletes the respective tab */
                        TabController.deleteTab(tabTitle) 
                    },
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
            className:= "tab-bar",

            /* Renders tabs */ 
            children <-- TabController.getFileNames.signal.map(names =>
                names.map(name => {
                    tabButton(name, TabController.getSelectedTab)
                })
            )
        )
    }
}