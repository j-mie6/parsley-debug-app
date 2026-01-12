package controller.viewControllers

import com.raquo.laminar.api.L.*
import model.DebugNode

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {
    
    /** The input element to be render by InputView */
    private val input: Var[Option[String]] = Var(None)

    /** Set input string */
    val setInput: Observer[String] = input.someWriter

    /** A reactive variable that determines whether the input panel is open */
    private val inputOpen: Var[Boolean] = Var(false)

    /** Retrieves a signal indicating whether the settings panel is open */
    def isInputOpen: Signal[Boolean] = inputOpen.signal
    def toggleOpen: Observer[Unit] = inputOpen.updater((old, _) => !old)
    
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
            case (Some(input), None) => div(
                className := "debug-input-container", 
                renderInputString(input)
            )

            /* Render as Input View with selected text corresponding to node */
            case (Some(input), Some(selected)) => div(
                className:= "debug-input-container", 
                renderInput(input, selected)
            )
        })


    private def renderInput(input: String, selected: DebugNode): HtmlElement = {
        val empty: Boolean = selected.inputStart == selected.inputEnd;

        /* Ensures correct text rendering */
        div(
            renderInputString(input.slice(0, selected.inputStart)),
            span(
                className := "debug-input selected",
                
                cls("empty") := empty,
                cls("fail") := !selected.success,
                cls("iterative") := selected.isIterative,
                cls("debug") := selected.isBreakpoint,

                if (empty)
                    i(
                        className := "bi",
                        cls("bi-exclamation-lg") := !selected.success,
                        cls("bi-pause-fill") := selected.isBreakpoint
                    )
                else
                    input.slice(selected.inputStart, selected.inputEnd)
                
            ),
            renderInputString(input.slice(selected.inputEnd, input.length()))
        )
    }

    private def renderInputString(input: String) = {
        child(span(className := "debug-input", input)) := input.nonEmpty
    }
    
}