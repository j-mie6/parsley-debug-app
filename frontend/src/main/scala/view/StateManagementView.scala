package view

import com.raquo.laminar.api.L.*

import model.toast.StateApplied
import controller.ToastController
import controller.viewControllers.StateManagementViewController

object StateManagementView {
    def apply(): HtmlElement = {
        div(
            className := "state-sidepanel-container",
            cls("open") <-- StateManagementViewController.isStateOpen,

            div(
                className := ("state-sidepanel"),
                
                div(cls("state-header"),
                    h2("References")
                ),

                div(cls("state-contents"),
                    div()
                ),

                div(
                    cls("state-footer"),
                    button(className := "apply-state-button", "Apply", onClick --> (_ => 
                        

                        ToastController.setToast(StateApplied)
                    )),
                ),
            )
        )
    }
}