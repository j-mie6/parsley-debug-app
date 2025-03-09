package view

import com.raquo.laminar.api.L.*

import model.toast.StateApplied
import controller.ToastController
import controller.viewControllers.StateManagementViewController
import controller.viewControllers.TreeViewController
import controller.viewControllers.TreeViewController.treeExists
import controller.tauri.{Tauri, Command}


object StateManagementView {

    def renderReference(ref: (Int, String)): HtmlElement = {
        val refAddr: Int = ref._1
        val refValue: String = ref._2

        div(
            className := "single-ref-top-container",
            div(
                className := "single-ref-title-container",
                "Ref ",
                text <-- StateManagementViewController.getRefNumber(refAddr)
            ),
            div(
                "Current Value: ",
                text <-- StateManagementViewController.getRefValue(refAddr)
            ),
            div(
                "Original Value: ",
                StateManagementViewController.getOrigRefValue(refAddr)
            ),
            div(
                className := "single-ref-modify-container",
                input(
                    placeholder := "New value",
                    onInput.mapToValue --> { newRefValue =>
                        StateManagementViewController.updateLocalRefValue(refAddr, newRefValue)
                    }
                )
            )
        )
    }

    def apply(): HtmlElement = {
        div(
            className := "state-sidepanel-container",
            cls("open") <-- StateManagementViewController.isStateOpen,
            
            div(
                className := "state-sidepanel",
                div(cls("state-header"),
                    h2("References")
                ),

                child(div(className := "nothing-shown", "Nothing to show")) <-- StateManagementViewController.refsEmptySignal,


                div(cls("state-contents"),
                    children <-- StateManagementViewController.getRefs.signal.map(_.map(renderReference))
                ),

                div(
                    cls("state-footer"),
                    child(button(className := "apply-state-button", "Apply", onClick --> (_ => {
                        StateManagementViewController.getLocalRefs.foreach(StateManagementViewController.updateNewRefValue)
                        TreeViewController.setRefs(StateManagementViewController.getLocalRefs)
                        ToastController.setToast(StateApplied)
                    }))) <-- StateManagementViewController.refsEmptySignal.not,
                    child(button(className := "restore-original-state-button", "Restore Originals", onClick --> (_ => {
                        TreeViewController.resetRefs().collectRight --> StateManagementViewController.getRefsVar
                        ToastController.setToast(StateApplied)
                    }))) <-- StateManagementViewController.refsEmptySignal.not,
                ),
            )
        )
    }
}