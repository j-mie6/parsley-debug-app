package controller.viewControllers

import com.raquo.laminar.api.L.*

import controller.viewControllers.MainViewController

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {
    /* The input element to be render by InputView */
    private val inputElement: Var[HtmlElement] = Var(MainViewController.getNoTreeFound)

    /**
     * Gets the input string
     * 
     * @return An HTML element displaying the tree's input string
     */
    def getInput: HtmlElement = div(
        child <-- inputElement,
    )
    
    /**
     * Sets the input string
     * 
     * @param input The new input string
     */
    private def setInput(input: HtmlElement): Unit = inputElement.set(input)

    /**
     * Converts an input string to an HTML element
     * 
     * @param input The input string
     */
    def toInputElement(input: String): Unit = {
        setInput (
            h1(
                className := "parser-input",
                s"Parser Input: ${input}"
            )
        )
    }
}