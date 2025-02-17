package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import view.DebugViewPage
import view.InputViewPage
import view.TreeViewPage

object MainViewHandler {
    /* If the current view is the tree view element */
    private val isTreeView: Var[Boolean] = Var(true)

    /**
     * Gets the selected view element: "tree" or "input"
     * 
     * @return The selected view element
     */
    def getMainView: Signal[HtmlElement] = 
        isTreeView.signal.map(if (_) then TreeViewPage() else InputViewPage())

    /**
     * Sets if the current view is the tree view
     * 
     */
    def setIsTreeView(isTree: Boolean): Unit = isTreeView.set(isTree)

    /**
     * Gets if the view is the tree view 
     * 
     * @return True if the current view is the tree view element
     */
    def isTreeView(isTree: Boolean): Signal[Boolean] = {
        isTreeView.signal.map(_ == isTree)
    }
}