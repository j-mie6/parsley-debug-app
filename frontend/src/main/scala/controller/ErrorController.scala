package controller.errors

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps

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
    private val errorVar: Var[Option[DillException]] = Var(None)

    /* Used to set an error to the var */
    val setError: Observer[DillException] = errorVar.someWriter

    /* Used to set an optional error to the var */
    val setOptError: Observer[Option[DillException]] = errorVar.writer

    /* Get current error in the var */
    val getError: Signal[Option[DillException]] = errorVar.signal
    
    /* Signal containing the html element to display the current error */
    val getErrorElem: Signal[Option[HtmlElement]] = getError.map(_.map(_.displayElement))

    /* Sets the error var back to no errors, used when a warning is clicked to make it disappear */
    val clearError: Observer[Unit] = Observer((_: Unit) => errorVar.set(None))

    /* Maps an error passed by the backend to a frontend DillException object */
    def mapException(error: Throwable): DillException = {
        error match {
            /* Backend errors */
            case js.JavaScriptException(jsErr) => parseException(jsErr.toString.stripPrefix(": "))

            /* Unknown error if not from backend or frontend */
            case _ => new UnknownError(s"Unknown error: ${error.toString()}")
        }
    }

    /* Read error name and match against Error case object */
    def parseException(errorName: String): DillException = {
        println("Found error - parsing")
        errorName.trim match
            case "TreeNotFound" => TreeNotFound
            case "LockFailed" => LockFailed
            case "NodeNotFound" => NodeNotFound(0)
            case "SerialiseFailed" => SerialiseFailed
            case "ReadDirFailed" => ReadDirFailed
            case "ReadPathFailed" => ReadPathFailed
            case "StringContainsInvalidUnicode" => StringContainsInvalidUnicode
            case "SuffixNotFound" => SuffixNotFound
            case "EventEmitFailed" => EventEmitFailed
            case _ => new UnknownError(s"Unknown backend error: ${errorName}")
    }
    
}
