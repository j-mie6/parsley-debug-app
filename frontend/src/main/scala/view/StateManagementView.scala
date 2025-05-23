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
            className := "sidepanel-item-container",
            
            div(
                className := "sidepanel-item-title",
                text <-- StateManagementViewController.getRefNumber(refAddr)
                    .map(i => s"R${StateRef.subscriptInt(i)}")
            ),
            
            div("Current Value: ", span(className := "ref-value", text <-- StateManagementViewController.getRefValue(refAddr))),
            div("Original Value: ", span(className := "ref-value", StateManagementViewController.getOrigRefValue(refAddr))),
            
            input(
                placeholder := "New value",
                onInput.mapToValue --> { newRefValue =>
                    StateManagementViewController.updateLocalRefValue(refAddr, newRefValue)
                }
            ),
        )
    }

    def apply(): HtmlElement = {
        div(
            className := "sidepanel-container state",
            cls("open") <-- StateManagementViewController.isStateOpen,
            
            div(className := "sidepanel-header", "References"),

            child(div(
                className := "nothing-shown", 
                fontSize.px := 18,
                "Nothing to show"
            )) <-- StateManagementViewController.refsEmptySignal,

            div(
                className := "sidepanel-items-container",
                children <-- StateManagementViewController.getRefs.signal.map(_.map(renderReference))
            ),
            
            div(flexGrow := 1),

            div(    
                className := "sidepanel-footer",

                child(button("Apply", onClick --> (_ => {
                    StateManagementViewController.getLocalRefs.foreach(StateManagementViewController.updateNewRefValue)
                    TreeViewController.setRefs(StateManagementViewController.getLocalRefs)
                    ToastController.setToast(StateApplied)
                }))) <-- StateManagementViewController.refsEmptySignal.not,

                child(button("Restore Originals", onClick --> (_ => {
                    TreeViewController.resetRefs().collectRight --> StateManagementViewController.getRefsVar
                    StateManagementViewController.getOrigRefs.foreach(StateManagementViewController.updateNewRefValue)
                    ToastController.setToast(StateApplied)
                }))) <-- StateManagementViewController.refsEmptySignal.not,
            ),
        )
    }
}