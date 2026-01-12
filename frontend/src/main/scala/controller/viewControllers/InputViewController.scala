package controller.viewControllers

import com.raquo.laminar.api.L.*
import model.DebugNode
import view.InputView

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {
    
    /** The input element to be render by InputView */
    private val input: Var[Option[String]] = Var(None)

    /** Set input string */
    val setInput: Observer[String] = input.someWriter

    /** Retrieve signal for input */
    val getInput: Signal[Option[String]] = input.signal

    /** A reactive variable that determines whether the input panel is open */
    private val inputOpen: Var[Boolean] = Var(false)

    /** Retrieves a signal indicating whether the settings panel is open */
    def isInputOpen: Signal[Boolean] = inputOpen.signal

    /** Close the input sidepanel */
    def closeInput: Observer[Unit] = Observer(_ => inputOpen.set(false))

    /** Toggle whether the input sidepanel is open */
    def toggleOpen: Observer[Unit] = inputOpen.invertWriter
    
    /** Set input to None to stop rendering */
    def unloadInput: Observer[Unit] = Observer(_ => input.set(None))
    
    /** Retrieves the input consumed by a node */
    def getNodeInput(node: DebugNode): Signal[Option[String]] = 
        input.signal.map(_.map(_.slice(node.inputStart, node.inputEnd)))


    /** Render input block */
    def getInputElem: Signal[HtmlElement] = input.signal
        .combineWith(TreeViewController.getSelectedNode)
        .map(_ match {
            /* Default tree view when no tree is loaded */
            case (None, _) => div(className := "nothing-shown", "Nothing to show")

            /* Render as Input View without any selected text */
            case (Some(input), None) => InputView.renderInput(input)

            /* Render as Input View with selected text corresponding to node */
            case (Some(input), Some(selected)) => InputView.renderInput(input, selected)
        })
}