package controller.viewControllers

import com.raquo.laminar.api.L.*

/**
 * Object containing functions and variables defining the input view,
 * which displays the input string of the tree from Parsley Debug
 */
object InputViewController {
    
    /* The input element to be render by InputView */
    private lazy val input: Signal[Option[String]] = MainViewController.input
    
    def getInputElem: Signal[Option[HtmlElement]] = input.signal.map(_.map(
        (input: String) => div(
            className := "debug-input-container",
            div(
                className := "debug-input-main-view",
                span(input) /* Ensures correct text rendering */
            )
        )
    ))
    
}