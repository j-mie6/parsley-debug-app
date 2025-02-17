package controller

import com.raquo.laminar.api.L.*

object InputController {
    /* The input element to be render by InputView */
    private val inputElement: Var[HtmlElement] = Var(div())

    /**
     * Converts an input string to an HTML element
     * 
     * @param input The input string
     * @return An HTML element displaying the input string
     */
    def toInputElement(input: String): HtmlElement = {
            h1(
                className := "debug-tree-title",
                p("Parser Input : ", margin.px := 0, fontSize.px := 15,
                    fontStyle.italic, fontWeight.lighter),
                input
            )
    }

    /**
     * Gets the input string
     * 
     * @return An HTML element displaying the tree's input string
     */
    def getInput: HtmlElement = div(child <-- inputElement)
    
    /**
     * Sets the input string
     * 
     * @param newInput The new input string
     */

    def setInput(input: String): Unit = {
        inputElement.set(
            toInputElement(input)
        )
    }
}