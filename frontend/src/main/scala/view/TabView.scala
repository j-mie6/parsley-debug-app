package view

import scala.util.Try

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.DebugTree
import controller.tauri.{Tauri, Command}
import controller.viewControllers.{TabViewController, TreeViewController, InputViewController}


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
        button(
            className := "close-tab-button",

            /* Close 'X' icon */
            i(className := "bi bi-x"),

            /* Deletes the respective tab */
            onClick(event => event.sample(TabViewController.getFileName(index))
                .flatMapMerge(TabViewController.deleteSavedTree)
                .tapEach(_ => EventStream.fromValue(0) --> TabViewController.setSelectedTab)
            ) --> TabViewController.setFileNames,
        )
    }

    /* Get selected file name as possible error */
    val selectedTab: EventStream[Try[String]] = TabViewController.getSelectedFileName.changes.recoverToTry
    
    def apply(): HtmlElement = {
        div(
            className:= "tab-bar",
            
            /* Update tree on new tab selected */  
            selectedTab.collectSuccess
                .flatMapMerge(TabViewController.loadSavedTree) 
                .collectLeft --> controller.errors.ErrorController.setError,
                // --> Observer.empty,

            /* If no tab can be found, unload tree from frontend */  
            selectedTab.collectFailure.mapToUnit --> TreeViewController.unloadTree,

            /* Renders tabs */ 
            children <-- TabViewController.getFileNames.signal.map(
                _.indices.map(tabButton(_))
            )
        )
    }
}