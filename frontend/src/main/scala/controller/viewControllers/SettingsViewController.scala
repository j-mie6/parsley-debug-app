package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps

import model.settings.*

/**
 * Controller for managing the visibility and values of user settings.
 * This object provides methods to toggle the settings panel, retrieve and update user settings,
 * and apply new configurations.
 */
object SettingsViewController {

    /**
     * A reactive variable that determines whether the settings panel is open.
     */
    private val settingsOpen: Var[Boolean] = Var(false)

    /**
     * Global store of user-configurable settings.
     */
    private val numSkipIterativeChildren: Var[Int] = Var(5) // Default: 5
    private val numSkipBreakpoints: Var[Int] = Var(1)       // Default: 1
    private val colorBlindMode: Var[Boolean] = Var(false)   // Default: false

    /**
     * Retrieves a signal indicating whether the settings panel is open.
     * 
     * @return A Signal[Boolean] representing the open state of the settings panel.
     */
    def isSettingsOpen: Signal[Boolean] = settingsOpen.signal

    /**
     * Toggles the visibility of the settings panel.
     */
    def toggleOpenSettings(): Unit = settingsOpen.set(!settingsOpen.now())
    
    /**
     * Retrieves the variable for the number of iterative children to skip.
     * 
     * @return A Var[Int] representing the current setting value.
     */
    def getNumSkipIterativeChildren: Var[Int] = numSkipIterativeChildren

    /**
     * Retrieves the variable for the number of breakpoints to skip.
     * 
     * @return A Var[Int] representing the current setting value.
     */
    def getNumSkipBreakpoints: Var[Int] = numSkipBreakpoints

    /**
     * Retrieves the variable for the colour blind mode setting.
     * 
     * @return A Var[Boolean] indicating whether colour blind mode is enabled.
     */
    def getColorBlindMode: Var[Boolean] = colorBlindMode

    /**
     * Updates the number of iterative children to be skipped.
     * 
     * @param newNumSkipIterativeChildren The new value to be set.
     */
    private def setNumSkipIterativeChildren(newNumSkipIterativeChildren: Int): Unit = 
        numSkipIterativeChildren.set(newNumSkipIterativeChildren)
        println(s"Set new iterative: ${newNumSkipIterativeChildren}")

    /**
     * Updates the number of breakpoints to be skipped.
     * 
     * @param newNumSkipBreakpoints The new value to be set.
     */
    private def setNumSkipBreakpoints(newNumSkipBreakpoints: Int): Unit = 
        numSkipBreakpoints.set(newNumSkipBreakpoints)
        println(s"Set new breakpoints: ${newNumSkipBreakpoints}")

    /**
     * Updates the colour blind mode setting.
     * 
     * @param newColorBlindMode The new Boolean value to be set.
     */
    private def setColorBlindMode(newColorBlindMode: Boolean): Unit = 
        colorBlindMode.set(newColorBlindMode)
        println(s"Set new colour blind mode: ${newColorBlindMode}")

    /**
     * Applies new user settings by updating the respective values and propagating changes.
     * 
     * @param newNumSkipIterativeChildren The new value for the number of iterative children to skip.
     * @param newNumSkipBreakpoints The new value for the number of breakpoints to skip.
     * @param newColorBlindMode The new value for the colour blind mode setting.
     */
    def applySettings(newNumSkipIterativeChildren: Int, newNumSkipBreakpoints: Int, newColorBlindMode: Boolean): Unit = {
        setNumSkipIterativeChildren(newNumSkipIterativeChildren)
        setNumSkipBreakpoints(newNumSkipBreakpoints)
        setColorBlindMode(newColorBlindMode)

        updateAllUserSettings(
            newNumSkipIterativeChildren = newNumSkipIterativeChildren,
            newNumSkipBreakpoints = newNumSkipBreakpoints,
            newColorBlindMode = newColorBlindMode
        )
    }
}
