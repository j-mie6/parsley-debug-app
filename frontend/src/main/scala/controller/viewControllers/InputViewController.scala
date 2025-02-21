package controller.viewControllers

import com.raquo.laminar.api.L.*

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {

    /* The input element to be render by InputView */
    private val inputElement: Var[String] = Var("Please attach a parser to DillRemoteView")

    
    /**
     * Gets the input string
     * 
     * @return An Signal holding the tree's input string
     */
    def getInput: Signal[String] = inputElement.signal
    
    /**
     * Sets the input string
     * 
     * @param input The new input string
     */
    val setInput = inputElement.writer

}