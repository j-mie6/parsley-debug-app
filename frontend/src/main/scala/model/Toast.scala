package model.toast

import com.raquo.laminar.api.L.*

import controller.errors.ErrorController
import com.raquo.laminar.api.features.unitArrows


/**
  * Toast is the generic frontend representation of any toast that the user sees
  * 
  * @param name Header of the toast
  * @param message More detailed message displayed to the user
  * @param icon Class name of the bootstrap icon relating to the pop up style
  * @param style CSS style used for toast, blue for info and green for success
  */
sealed trait Toast {
    def name: String
    def message: String
    def icon: String
    def style: String

    def displayElement: HtmlElement = {
        div(
            cls("toast", style),

            div(
                cls("toast-icon-container", style),
                i(className:= icon, width.px := 30, height.px := 30)
            ),

            div(
                h3(className:= "toast-header", name),
                div(className := "toast-text", message),
            ),

            onClick.mapTo(None) --> ErrorController.setOptError,
        )
    }
}


/**
  * The generic toast for information that needs to be passed to the user, styled in blue
  */
sealed trait InfoToast extends Toast {
    override def style: String = "info" 
    override def icon: String = "bi bi-info-circle-fill"
}

/**
  * The generic toast for success, styled in green
  */
sealed trait SuccessToast extends Toast  {
    override def name: String = "Success"
    override def style: String = "success"
    override def icon: String = "bi bi-check-circle-fill"
}

/**
  * The generic toast for error, styled in red
  */
sealed trait ErrorToast extends Toast {
    override def style: String = "error" 
    override def icon: String = "bi bi-exclamation-triangle-fill"
}

case object TreeDownloaded extends SuccessToast {
    override def message: String = "Tree downloaded"
}

case object SettingsApplied extends SuccessToast {
    override def message: String = "Settings applied"
}

case object DefaultSettingsApplied extends SuccessToast {
    override def message: String = "Default settings restored"
}

case object StateApplied extends SuccessToast {
    override def message: String = "State applied"
}

case object TreeDownloaded extends SuccessToast {
    override def message: String = "Tree downloaded"
}

case object TreeUploaded extends SuccessToast {
    override def message: String = "Tree uploaded"
}

case object TreeImportFailed extends ErrorToast {
    override def message: String = "Tree importing failed, this probably means the file is malformed"
}

