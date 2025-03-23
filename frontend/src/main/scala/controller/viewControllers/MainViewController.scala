package controller.viewControllers

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import view.DebugViewPage
import view.InputView
import view.TreeView
import view.CodeView
import model.DebugTree
import model.DebugNode

/**
 * Object containing functions and variables for the main view,cconditionally
 * rendering the tree or input view depending on the user's selection
 */
object MainViewController {
    
    private val state: Var[Option[DebugTree]] = Var(None)

    lazy val setState: Observer[DebugTree] = state.someWriter
    lazy val tree = state.signal 

    private lazy val root: Var[Option[DebugNode]] 
        = state.zoomLazy(_.map(_.root))((tree, root) => tree.zip(root).map((t, r) => t.copy(root = r)))
    
    lazy val input = inputVal.signal 
    private lazy val inputVal: Var[Option[String]] 
        = state.zoomLazy(_.map(_.input))((tree, input) => tree.zip(input).map((t, i) => t.copy(input = i)))
        
    lazy val parserInfo = parserInfoVal.signal 
    private lazy val parserInfoVal: Var[Option[Map[String, List[(Int, Int)]]]] 
        = state.zoomLazy(_.map(_.parserInfo))((tree, parserInfo) => tree.zip(parserInfo).map((t, p) => t.copy(parserInfo = p)))


    val unload: Observer[Unit] = Observer {
        _ => state.set(None) 
    }
        
    /* View selected */
    enum View(val elem: HtmlElement) {
        case Tree extends View(TreeView())
        case Input extends View(InputView())
        case Code extends View(CodeView())
    }

    /* Current view selected */
    private val view: Var[View] = Var(View.Tree)

    private def renderButton(buttonType: String, icon: String, text: String, view: View, openSemaphore: Var[Int]): HtmlElement = button(
        className := f"debug-view-select-button debug-view-${buttonType}-button",
        i(className := f"bi bi-${icon}"),
        
        cls("selected") <-- MainViewController.view.signal.map(_ == view),

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

    /** Set selected view */
    val setView: Observer[View] = view.writer

    /** Get selected view element */
    val getView: Signal[HtmlElement] = view.signal.withCurrentValueOf(state.signal)
        .map((view, state) => if state.isDefined then view.elem else nothingShown)
        
    lazy val nothingShown: HtmlElement = {
        div(
            className := "nothing-shown",
            "Nothing to show"
        )
    }
}