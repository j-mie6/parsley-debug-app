package controller.viewControllers

import com.raquo.laminar.api.L.*

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {

    /* The input element to be render by InputView */
    private val input: Var[String] = Var("Please attach a parser to DillRemoteView")

    /** Get input string */
    def getInput: Signal[String] = input.signal

    /** Set input string */
    def setInput: Observer[String] = input.writer

}