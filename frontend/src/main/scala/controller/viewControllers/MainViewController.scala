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
    /* View selected */
    enum View(val elem: HtmlElement) {
        case Tree extends View(TreeView())
        case Input extends View(InputView())
    }

    /* Current view selected */
    private val view: Var[View] = Var(View.Tree)

    /* Get view */
    def getView: Signal[View] = view.signal

    /* Set selected view */
    def setView: Observer[View] = view.writer

    /* Gets selected view element */
    def getViewElem: Signal[HtmlElement] = view.signal.map(_.elem)

}