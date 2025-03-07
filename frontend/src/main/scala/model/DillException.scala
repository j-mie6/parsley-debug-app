package model.errors

import com.raquo.laminar.api.L.*

import controller.errors.ErrorController
import com.raquo.laminar.api.features.unitArrows


/**
  * Popup is the generic frontend representation of any pop ups that the user sees
  * 
  * @param name Title of a pop up
  * @param message More detailed message displayed to the user
  * @param closable Whether the popup can be cleared by clicking on it (non-breaking warnings)
  * @param icon Class name of the bootstrap icon relating to the pop up style
  * @param style css style used for popup yellow for warnings and red for errors
  * 
  */
sealed trait Popup {
    def name: String
    def message: String
    def closable: Boolean
    def icon: String
    def style: String

    def displayElement: HtmlElement = {
        div(
            cls("popup", style),

            div(
                cls("popup-icon-container", style),
                i(className:= icon, width.px := 30, height.px := 30)
            ),
            
            div(
                h2(className := "popup-header",name),
                div(className := "popup-text", embedGithubIssue(message)),
            ),

            onClick.mapTo(None).filter(_ => closable) --> ErrorController.setOptError,
        )
    }
}

/**
  * DillException is the generic frontend representation of an Exception
  */
sealed trait DillException extends Popup

/**
  * Represents a non-breaking Exception in Dill, styled in yellow and can be closed
  */
sealed trait Warning extends DillException {
    override def closable: Boolean = true
    override def style: String = "warning"
    override def icon: String = "bi bi-exclamation-circle-fill"
}

/**
  * Represents a breaking Exception in Dill, styled in red and cannot be closed
  */
sealed trait Error extends DillException {
    override def closable: Boolean = false
    override def style: String = "error"
    override def icon: String = "bi bi-exclamation-triangle-fill"
}

/**
  * Checks for the string "Github Issue" and replaces it with a link to 
  * create a Github Issue in the Dill repository
  *
  * @param msg Full message that we are checking on
  * @return Github Issue as a link HTML element if "Github Issue" is in the message
  */
def embedGithubIssue(msg: String): HtmlElement = {
    val githubLink = a(
        href := "https://github.com/j-mie6/parsley-debug-app/issues/new",
        target := "_blank",
        "GitHub Issue",
    )

    if (msg.contains("Github Issue")) then
        val parts = msg.split("GitHub Issue")
        span(parts.head, githubLink, parts.last)
    else
        span(msg)
}

/* List of DILL Exceptions */

case object TreeNotFound extends Error {
    override def name: String = "Tree Not Found"
    override def message: String = 
        "The application server failed to retrieve the debug tree from Remote View. " +
        "Ensure that the debug session is active and reachable. If the problem persists, please" + 
        "write a GitHub Issue on the repository"
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
        "Please report this issue a GitHub Issue if the problem persists"
}

case object ReadDirFailed extends Error {
    override def name: String = "Directory Read Failure"
    override def message: String = 
        "The system encountered an error while attempting to read a directory. Ensure the directory exists."
}

case object ReadPathFailed extends Warning {
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

case object EventEmitFailed extends Error {
    override def name: String = "Event Emitting Failed"
    override def message: String = "An event could not be emitted from the backend. Try again."
}

case object MalformedJSON extends Error {
    override def name: String = "Malformed JSON Received"
    override def message: String = 
        "Failed to serialize the display tree. " +
        "This may indicate an issue with the serialization process. " +
        "Please report this bug through a GitHub Issue if the problem persists"
}

/* Used when an unexpected error occurs */
case class UnknownError(msg: String) extends Error {
    override def name: String = "Unknown Error"
    override def message: String = s"Something unexpected went wrong: $msg"
}