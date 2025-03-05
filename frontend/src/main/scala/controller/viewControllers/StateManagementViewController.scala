package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps


object StateManagementViewController {
    private val stateOpen: Var[Boolean] = Var(false)

    /**
     * Retrieves a signal indicating whether the state management panel is open.
     * 
     * @return A Signal[Boolean] representing the open state of the state management panel.
     */
    def isStateOpen: Signal[Boolean] = stateOpen.signal

    /**
     * Toggles the visibility of the state management panel.
     */
    def toggleOpenState(): Unit = stateOpen.set(!stateOpen.now())
}