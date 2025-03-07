package controller.viewControllers

import com.raquo.laminar.api.L.*

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {

    /* The input element to be render by InputView */
    private val input: Var[Option[String]] = Var(None)

    /** Set input string */
    val setInput: Observer[String] = input.someWriter

    /** Set input to None to stop rendering */
    def unloadInput: Observer[Unit] = Observer(_ => input.set(None))

    def getInputElem: Signal[HtmlElement] = input.signal.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => 
          div(
            className := "tree-view-error",
            "Nothing to show"
          )

        /* Render as DebugTreeDisplay */
        case Some(inputString) => div(className:= "debug-input-main-view", inputString)
    )

}