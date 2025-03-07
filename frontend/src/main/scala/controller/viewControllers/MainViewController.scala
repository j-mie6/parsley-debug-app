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
        case Code extends View(InputView()) // TODO
    }

    /* Current view selected */
    private val view: Var[View] = Var(View.Tree)

    private def renderButton(buttonType: String, icon: String, text: String, view: View, openSemaphore: Var[Int]): HtmlElement = button(
        className := f"debug-view-select-button debug-view-${buttonType}-button",
        i(className := f"bi bi-${icon}"),
        
        cls("selected") <-- MainViewController.getView.map(_ == view),

        div(
            className := f"debug-view-expand-button debug-view-expand-${buttonType}",
            cls("expanded") <-- openSemaphore.signal.map(_ > 0),
            p(text, marginLeft.px := 5),
        ),

        onClick.preventDefault.mapTo(view) --> MainViewController.setView,
    )

    def renderViewButton(view: View, openSemaphore: Var[Int]): HtmlElement = view match {
        case View.Tree =>   renderButton("tree", "tree-fill", "Tree View", View.Tree, openSemaphore)
        case View.Input =>  renderButton("input", "file-earmark-text-fill", "Input View", View.Input, openSemaphore)
        case View.Code =>   renderButton("code", "file-earmark-code-fill", "Code View", View.Code, openSemaphore)
    }

    /** Get current selected view */
    val getView: Signal[View] = view.signal

    /** Set selected view */
    val setView: Observer[View] = view.writer

    /** Get selected view element */
    val getViewElem: Signal[HtmlElement] = view.signal.map(_.elem)

}