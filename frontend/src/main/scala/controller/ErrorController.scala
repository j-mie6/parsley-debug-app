package controller.errors

import scala.scalajs.js

import com.raquo.laminar.api.L.*

import controller.MalformedJSONException

import model.errors.*

/** 
  * ErrorController keeps track of the error state of the app 
  * and the code to display error popups in the app 
  */
object ErrorController {
    
    /**
      * Icon to show a popup is a warning
      */
    private lazy val alertIcon: Element = i (
        className := "bi bi-exclamation-triangle-fill", 
        fontSize.px := 30,
    )

    /* Holds current error information */
    val errorVar: Var[Option[DillException]] = Var(None)
    
    /* EmptyNode will not be rendered when there isn't an error */
    val displayError: HtmlElement = div(
        child <-- errorVar.signal.map(_.map(err => err.displayElement).getOrElse(emptyNode)),
    )

    /* Maps an error passed by the backend to a frontend DillException object */
    def mapException(error: Throwable): DillException = {
        error match {
            /* Backend errors */
            case js.JavaScriptException(jsErr) => jsErr.toString.stripPrefix(": ") match {
                case "TreeNotFound" => TreeNotFound
                case "LockFailed" => LockFailed
                case "NodeNotFound" => NodeNotFound(0)
                case "SerialiseFailed" => SerialiseFailed
                case "ReadDirFailed" => ReadDirFailed
                case "ReadPathFailed" => ReadPathFailed
                case "StringContainsInvalidUnicode" => StringContainsInvalidUnicode
                case "SuffixNotFound" => SuffixNotFound

                case _ => new UnknownError(s"Unknown backend error: ${jsErr.toString()}")
            }

            /* Frontend errors */
            case MalformedJSONException => MalformedJSON
            
            /* Unknown error if not from backend or frontend */
            case _ => new UnknownError(s"Unknown error: ${error.toString()}")
        }
    }

    /* Updates the error var with error that has been passed by the backend */
    def handleException(error: Throwable): Unit = {
        println(s"Error was: ${error.toString()}")
        errorVar.set(Some(mapException(error)))
    }

    /* Sets the error var back to no errors, used when a warning is clicked to make it disappear */
    def clearError(): Unit = {
        errorVar.set(None)
    }
}