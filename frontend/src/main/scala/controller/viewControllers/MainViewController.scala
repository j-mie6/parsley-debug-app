package controller.viewControllers

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import view.DebugViewPage
import view.InputView
import view.TreeView

/**
 * Object containing functions and variables for the main view,cconditionally
 * rendering the tree or input view depending on the user's selection
 */
object MainViewController {
    /* If the current view is the tree view element */
    private val isTreeView: Var[Boolean] = Var(true)

    /* Default tree view when no tree is loaded */
    private val noTreeFound: HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )

    /**
     * Gets the selected view element: "tree" or "input"
     * 
     * @return The selected view element
     */
    def getMainView: Signal[HtmlElement] = 
        isTreeView.signal.map(if (_) then TreeView() else InputView())

    def getNoTreeFound: HtmlElement = noTreeFound

    /**
     * Sets if the current view is the tree view
     * 
     * @param isTree True if the current view is the tree view element
     */
    def setIsTreeView(isTree: Boolean): Unit = isTreeView.set(isTree)

    /**
     * Gets if the view is the tree view 
     * 
     * @param isTree True if the current view is the tree view element
     * @return True if the comparing view with tree view element,
     * false for input
     */
    def isTreeView(isTree: Boolean): Signal[Boolean] = 
        isTreeView.signal.map(_ == isTree)
    
}