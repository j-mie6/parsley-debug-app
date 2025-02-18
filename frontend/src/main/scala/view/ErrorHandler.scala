package view.error

import com.raquo.laminar.api.L.*

import view.error.*

/** 
  * ErrorHandler keeps track of the error state of the app and the code to display error 
  * popups in the app 
  */
object ErrorHandler {
    
    private lazy val alertIcon: Element = i (
        className := "bi bi-exclamation-triangle-fill", 
        fontSize.px := 30,
    )

    /* Holds current error information */
    val errorVar: Var[Option[DillException]] = Var(None)
    
    /* EmptyNode will not be rendered (if there is no error) */
    val displayError: HtmlElement = div(
        child <-- errorVar.signal.map(_.map(_.displayElement()).getOrElse(emptyNode)),
    )

    /* Maps an error passed by the backend to a frontend DillException object */
    /* I think the problem is errors are scala.js errors, so they are all 
       js.JavaScriptException: https://www.scala-js.org/doc/interoperability/exceptions.html */
    def mapError(error: Throwable): DillException = {
        error match {
            case _ => new PracticeWarning("hey", "hi")
        }
    }

    /* Updates the error var with error that has been passed by the backend */
    def handleError(error: Throwable): Unit = {
        println(s"Error was: ${error.toString()}")
        errorVar.set(Some(mapError(error)))
    }

    /* Sets the error var back to no errors, used when a warning is clicked to make it disappear */
    def clearError(): Unit = {
        errorVar.set(None)
    }

}