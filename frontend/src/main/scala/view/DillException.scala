package view.error

import com.raquo.laminar.api.L.*

sealed trait DillException {
    def name: String
    def message: String
    def canDelete: Boolean
    def colour: String

    def displayElement: HtmlElement = {
        div(
            className := "popup",
            h2(
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.name),
                color := this.colour,
            ),
            br(),
            div(
                className := "popup-text",
                child.text <-- ErrorHandler.errorVar.signal.map(_.get.message),
            ),
            display <-- ErrorHandler.errorVar.signal.map(opt => if (opt.isDefined) "block" else "none"),
            if canDelete then onClick --> {_ => ErrorHandler.errorVar.set(None)}
        )
    }
}

sealed trait Warning extends DillException {
    override def colour: String = "ffff00"
    override def canDelete: Boolean = true
}

sealed trait Error extends DillException {
    override def colour: String = "ff0000"
    override def canDelete: Boolean = false
}

/* 
    DILL ERRORS
*/

case object TreeNotFound extends Error {
    override def name: String = "Tree Not Found"
    override def message: String = "App server could not retrieve debug tree from Remote View"
}

case object LockFailed extends Error {
    override def name: String = "Lock Failed"
    override def message: String = "App thread could not get a hold of the app lock. Please re-start the app"
}

case class NodeNotFound(nodeId: Int) extends Error {
    override def name: String = "Node Not Found"
    override def message: String = s"Child node with ID $nodeId does not exist."
}

case object SerialiseFailed extends Error {
    override def name: String = "Serialise Failed"
    override def message: String = "Could not serialise display tree. Please contact Jamie about this"
}

case object ReadDirFailed extends Error {
    override def name: String = "Read Directory Failed"
    override def message: String = "Failed to read directory"
}

case object ReadPathFailed extends Error {
    override def name: String = "Read Path Failed"
    override def message: String = "Failed to read path"
}

case object StringContainsInvalidUnicode extends Error {
    override def name: String = "String Contains Invalid Unicode"
    override def message: String = "Remove invalid character"
}

case object SuffixNotFound extends Error {
    override def name: String = "Suffix Not Found"
    override def message: String = "Could not find suffix"
}

case object MalformedJSON extends Error {
    override def name: String = "Malformed JSON"
    override def message: String = "Frontend received malformed JSON"
}

case class UnknownError(message: String) extends Error {
    override def name: String = "Unknown Error"
    override def message: String = s"Something went wrong: $message"
}

case object PracticeWarning extends Warning {
    override def name: String = "Practice Warning"
    override def message: String = "This is a practice warning, this should not be on release"
}