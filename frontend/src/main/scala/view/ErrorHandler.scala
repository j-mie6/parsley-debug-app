package view.error

import com.raquo.laminar.api.L.*

import view.error.*
// import web.syntax.visibilitySwitch

object ErrorHandler {
    
    private lazy val alertIcon: Element = i(
        className := "bi bi-exclamation-triangle-fill", 
        fontSize.px := 30,
        )
    val errorVar: Var[Option[Throwable]] = Var(None)
    
    val displayError: HtmlElement = 
      div(
        className := "popup",
        h2(
            "Something went wrong",
            // child.text <-- errorVar.signal.map(_.getOrElse(Exception("Err")).toString),
            color := "#ffffff",
        ),
        br(),
        div(
            className := "popup-text",
            child <-- errorVar.signal.map(getErrorMessage(_)),//.map(getErrorMessage(_)),
        ),
        display <-- errorVar.signal.map(opt => if (opt.isDefined) "block" else "none"),
        // onClick --> {_ => errorVar.set(None)}
      )
    
    def getErrorMessage(error: Option[Throwable]): String = {
        if (!error.isDefined) {
            return "None"
        }

        var errorName = error.get.toString().split(" ").last
        
        errorName = errorName match {
            case "LockFailed" => ""
            case "NodeNotFound" => ""
            case "SerialiseFailed" => ""
            case "TreeNotFound" => "App server could not retrieve debug tree from Remote View"
            case "ReadDirFailed" => ""
            case "ReadPathFailed" => ""
            case "StringContainsInvalidUnicode" => ""
            case "SuffixNotFound" => ""
            case _ => "Something unexpected went wrong"
        }

        return errorName
        // error match {
        //     case 
        // }
    }

    def handleError(error: Throwable): Unit = {
        println(s"Error was: ${error.toString()}")
        errorVar.set(Some(error))
    }
}