package view.error

import com.raquo.laminar.api.L.*

import view.error.*
// import web.syntax.visibilitySwitch

object ErrorHandler {
    
    private lazy val alertIcon: Element = i (
        className := "bi bi-exclamation-triangle-fill", 
        fontSize.px := 30,
    )

    val errorVar: Var[Option[DillException]] = Var(None)
    
    val displayError: HtmlElement = errorVar.now().getOrElse(DefaultWarning).displayElement()

    def mapError(error: Throwable): DillException = {
        error match {
            case _ => new PracticeWarning("hey", "hi")
        }
    }

    def handleError(error: Throwable): Unit = {
        println(s"Error was: ${error.toString()}")
        errorVar.set(Some(mapError(error)))
    }

}