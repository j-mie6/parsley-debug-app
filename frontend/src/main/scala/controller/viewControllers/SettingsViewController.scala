package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps

import model.settings.*

object SettingsViewController {

    private val settingsOpen: Var[Boolean] = Var(false)

    /* Store of global values for user settings */
    private val numSkipIterativeChildren: Var[Int] = Var(5)
    private val numSkipBreakpoints: Var[Int] = Var(1)
    private val colorBlindMode: Var[Boolean] = Var(false)

    def isSettingsOpen: Signal[Boolean] = settingsOpen.signal

    def toggleOpenSettings(): Unit = settingsOpen.set(!settingsOpen.now())
    
    def getNumSkipIterativeChildren: Var[Int] = numSkipIterativeChildren

    def getNumSkipBreakpoints: Var[Int] = numSkipBreakpoints

    def getColorBlindMode: Var[Boolean] = colorBlindMode

    def setNumSkipIterativeChildren(newNumSkipIterativeChildren: Int): Unit = 
        numSkipIterativeChildren.set(newNumSkipIterativeChildren)

    def setNumSkipBreakpoints(newNumSkipBreakpoints: Int): Unit = 
        numSkipBreakpoints.set(newNumSkipBreakpoints)

    def setColorBlindMode(newColorBlindMode: Boolean): Unit = 
        colorBlindMode.set(newColorBlindMode)

    def applySettings(newNumSkipIterativeChildren: Int, newNumSkipBreakpoints: Int, newColorBlindMode: Boolean): Unit = {
        setNumSkipIterativeChildren(newNumSkipIterativeChildren)
        setNumSkipBreakpoints(newNumSkipBreakpoints)
        setColorBlindMode(newColorBlindMode)
    }
}