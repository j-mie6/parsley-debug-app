package controller.viewControllers

import com.raquo.laminar.api.L.*

object InputViewController {
    /* The input element to be render by InputView */
    private val inputElement: Var[String] = Var("Please attach a parser to DillRemoteView")

    /**
     * Gets the input string
     * 
     * @return An HTML element displaying the tree's input string
     */
    def getInput: Var[String] = inputElement
    
    /**
     * Sets the input string
     * 
     * @param input The new input string
     */
    def setInput(input: String): Unit = {
        inputElement.set(input)
    }
}