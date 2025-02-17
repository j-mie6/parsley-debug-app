package view.error

import com.raquo.laminar.api.L.*

sealed trait DillException(name: String, message: String) {
    def getMessage: String = message
    def getName: String = name


    def displayElement(): HtmlElement
}

sealed trait Warning(name: String, message: String) extends DillException(name: String, message: String) {
    override def displayElement(): HtmlElement = {
        div(
            className := "popup",
            h2(
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.getName),
                color := "#ffffff",
            ),
            br(),
            div(
                className := "popup-text",
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.getMessage),
            ),
            display <-- ErrorHandler.errorVar.signal.map(opt => if (opt.isDefined) "block" else "none"),
            onClick --> {_ => ErrorHandler.errorVar.set(None)}
        )
    }
}

case class PracticeWarning(name: String, message: String) extends Warning(name: String, message: String)

sealed trait Error(name: String, message: String) extends DillException(name: String, message: String) {
    override def displayElement(): HtmlElement = {
        div(
            className := "popup",
            h2(
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.getName),
                color := "#ffffff",
            ),
            br(),
            div(
                className := "popup-text",
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.getMessage),
            ),
            display <-- ErrorHandler.errorVar.signal.map(opt => if (opt.isDefined) "block" else "none"),
      )
    }
}

// case class TreeNotFound("Tree Not Found", "App server could not retrieve debug tree from Remote View") extends Error
// case class LockFailed("Lock Failed", "App thread could not get a hold of the app lock. Please re-start the app") extends Error
// case class NodeNotFound("Node Not Found", "Child does not exist", 62) extends Error

// case class SerialiseFailed("Serialise Failed", "Could not serialise display tree. Please contact Jamie about this") extends Error
// case class ReadDirFailed("Read Directory Failed", "Failed to read directory") extends Error
// case class ReadPathFailed("Read Path Failed", "Failed to read path") extends Error
// case class StringContainsInvalidUnicode("String Contains Invalid Unicode", "Remove invalid character") extends Error
// case class SuffixNotFound("Suffix Not Found", "Could not find suffix") extends Error
// case class MalformedJSON("Malformed JSON", "Frontend received malformed JSON") extends Error