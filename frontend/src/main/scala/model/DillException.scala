package model.errors

import com.raquo.laminar.api.L.*

import controller.errors.ErrorController

/**
  * DillException is the generic frontend representation of an Exception
  * 
  * @param name Name of Exception
  * @param message More detailed error message displayed to the user
  * @param canDelete Whether the popup can be cleared by clicking on it (only for non-breaking warnings)
  * @param colour Outline colour for popup, red for errors and yellow for warnings
  */
sealed trait DillException {
    def name: String
    def message: String
    def closable: Boolean
    def style: String

    def displayElement: HtmlElement = {
        div(
            className := "popup",
            cls := style,
            h2(
                this.name,
                color := "#ffffff",
            ),
            br(),
            div(
                className := "popup-text",
                this.message,
            ),
            if closable then onClick --> {_ => ErrorController.clearError()} else onClick --> {_ => ()}
        )
    }
}

/**
  * Represents a non-breaking Exception in Dill. Overrides colour to yellow and canDelete to true
  */
sealed trait Warning extends DillException {
    override def closable: Boolean = true
    override def style: String = "warning"
}

/**
  * Represents a breaking Exception in Dill. Overrides colour to red and canDelete to false
  */
sealed trait Error extends DillException {
    override def closable: Boolean = false
    override def style: String = "error"
}

/* List of DILL Exceptions */

case object TreeNotFound extends Error {
    override def name: String = "Tree Not Found"
    override def message: String = 
        "The application server failed to retrieve the debug tree from Remote View. " +
        "Ensure that the debug session is active and reachable. If the problem persists, please" + 
        "contact Jamie Willis."
}

case object LockFailed extends Error {
    override def name: String = "Lock Acquisition Failed"
    override def message: String = 
        "The application thread was unable to acquire the required lock. Try restarting the application"
}

case class NodeNotFound(nodeId: Int) extends Error {
    override def name: String = "Node Not Found"
    override def message: String = 
        s"Could not locate child node with ID $nodeId."
}

case object SerialiseFailed extends Error {
    override def name: String = "Serialization Failed"
    override def message: String = 
        "Failed to serialize the display tree. " +
        "This may indicate an issue with the serialization process. " +
        "Please report this issue to Jamie Willis"
}

case object ReadDirFailed extends Error {
    override def name: String = "Directory Read Failure"
    override def message: String = 
        "The system encountered an error while attempting to read a directory. Ensure the directory exists."
}

case object ReadPathFailed extends Error {
    override def name: String = "Path Read Failure"
    override def message: String = 
        "Failed to access the specified file path. Ensure the path is correct and not blocked by another process."
}

case object StringContainsInvalidUnicode extends Error {
    override def name: String = "Invalid Unicode Character"
    override def message: String = 
        "The input string contains invalid Unicode characters. Ensure that all characters are properly encoded and remove any unsupported symbols."
}

case object SuffixNotFound extends Error {
    override def name: String = "Suffix Not Found"
    override def message: String = 
        "The expected suffix was not found in the input. Verify that the correct format is being used."
}

case object MalformedJSON extends Error {
    override def name: String = "Malformed JSON Received"
    override def message: String = 
        "Failed to serialize the display tree. " +
        "This may indicate an issue with the serialization process. " +
        "Please report this issue to Jamie Willis if the problem persists"
}

/* Used when an unexpected error occurs */
case class UnknownError(mes: String) extends Error {
    override def name: String = "Unknown Error"
    override def message: String = s"Something unexpected went wrong: $mes"
}