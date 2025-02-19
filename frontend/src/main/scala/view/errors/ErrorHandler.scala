package view.error

import com.raquo.laminar.api.L.*

import scala.scalajs.js

import view.error.*

import controller.MalformedJSONException

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
        child <-- errorVar.signal.map(_.map(err => err.displayElement).getOrElse(emptyNode)),
    )

    /* Maps an error passed by the backend to a frontend DillException object */
    def mapError(error: Throwable): DillException = {
        error match {
            /* Backend errors */
            case js.JavaScriptException(jsErr) => jsErr match {
                case err if err.toString.contains("TreeNotFound") => TreeNotFound
                case err if err.toString.contains("LockFailed") => LockFailed
                case err if err.toString.contains("NodeNotFound") => NodeNotFound(0)
                case err if err.toString.contains("SerialiseFailed") => SerialiseFailed
                case err if err.toString.contains("ReadDirFailed") => ReadDirFailed
                case err if err.toString.contains("ReadPathFailed") => ReadPathFailed
                case err if err.toString.contains("StringContainsInvalidUnicode") => StringContainsInvalidUnicode
                case err if err.toString.contains("SuffixNotFound") => SuffixNotFound

                case _ => new UnknownError(s"Unknown backend error: ${jsErr.toString()}")
            }

            /* Frontend errors */
            case MalformedJSONException => MalformedJSON
            
            /* Unknown error if not from backend or frontend */
            case _ => new UnknownError(s"Unknown error: ${error.toString()}")
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