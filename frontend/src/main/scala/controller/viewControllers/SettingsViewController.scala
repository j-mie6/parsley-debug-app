package controller.viewControllers

import scala.scalajs.js
import scala.scalajs.js.internal.UnitOps.unitOrOps

import com.raquo.laminar.api.L.*

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

    /* Default Values */
    val numSkipIterativeChildrenDefault: Int = 5
    val numSkipBreakpointsDefault: Int = 1
    val colorBlindModeDefault: Boolean = false

    /**
     * Global store of user-configurable settings.
     */
    private val numSkipIterativeChildren: Var[Int] = Var(numSkipIterativeChildrenDefault)
    private val numSkipBreakpoints: Var[Int] = Var(numSkipBreakpointsDefault)
    private val colorBlindMode: Var[Boolean] = Var(colorBlindModeDefault)

    /**
     * Retrieves a signal indicating whether the settings panel is open.
     * 
     * @return A Signal[Boolean] representing the open state of the settings panel.
     */
    def isSettingsOpen: Signal[Boolean] = settingsOpen.signal

    /**
     * Toggles the visibility of the settings panel.
     */
    def toggleOpen: Observer[Unit] = settingsOpen.updater((old, _) => !old)
    
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

    /**
     * Updates the number of breakpoints to be skipped.
     * 
     * @param newNumSkipBreakpoints The new value to be set.
     */
    private def setNumSkipBreakpoints(newNumSkipBreakpoints: Int): Unit = 
        numSkipBreakpoints.set(newNumSkipBreakpoints)

    /**
     * Updates the colour blind mode setting.
     * 
     * @param newColorBlindMode The new Boolean value to be set.
     */
    private def setColorBlindMode(newColorBlindMode: Boolean): Unit = 
        colorBlindMode.set(newColorBlindMode)

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
