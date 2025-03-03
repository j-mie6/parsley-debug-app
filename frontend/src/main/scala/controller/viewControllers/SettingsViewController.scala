package controller.viewControllers

import scala.scalajs.js

import com.raquo.laminar.api.L.*
import scala.scalajs.js.internal.UnitOps.unitOrOps

import model.settings.*

object SettingsViewController {

    private val numSkipIterativeChildren: Var[Int] = Var(5)
    private val numSkipBreakpoints: Var[Int] = Var(1)
    private val colorBlindMode: Var[Boolean] = Var(false)

    def getNumSkipIterativeChildren: Var[Int] = numSkipIterativeChildren

    def getNumSkipBreakpoints: Var[Int] = numSkipBreakpoints

    def getColorBlindMode: Var[Boolean] = colorBlindMode

    def setNumSkipIterativeChildren(num: Int): Unit = numSkipIterativeChildren.set(num)

    def setNumSkipBreakpoints(num: Int): Unit = numSkipBreakpoints.set(num)

    def setColorBlindMode(isColorBlind: Boolean): Unit = colorBlindMode.set(isColorBlind)
}