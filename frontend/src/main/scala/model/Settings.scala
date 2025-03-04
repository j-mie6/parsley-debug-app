package model.settings

import com.raquo.laminar.api.L.*

import controller.viewControllers.SettingsViewController
sealed trait UserSetting[T] {
    def settingName: String
    def infoText: String
    def value: Var[T]

    def setValue(newValue: T): Unit = {
        value.set(newValue)
    }
}

case class Settings(numSkipIterativeChildren: Int, numSkipBreakpoints: Int, colorBlindMode: Boolean)

object Settings {
    def empty = Settings(numSkipIterativeChildren = 5, numSkipBreakpoints = 1, colorBlindMode = false)

    val settings = Var[Settings](Settings.empty)

    val numSkipIterativeChildren =
        settings.zoomLazy(_.numSkipIterativeChildren)((settings, numSkipIterativeChildren) => 
            settings.copy(numSkipIterativeChildren = numSkipIterativeChildren)
    )
    val numSkipBreakpoints =
        settings.zoomLazy(_.numSkipBreakpoints)((settings, numSkipBreakpoints) => 
            settings.copy(numSkipBreakpoints = numSkipBreakpoints)
    )
    val colorBlindMode =
        settings.zoomLazy(_.colorBlindMode)((settings, colorBlindMode) => 
            settings.copy(colorBlindMode = colorBlindMode)
    )
}

case object NumSkipIterativeChildren extends UserSetting[Int] {
    override def settingName: String = "Iterative children to skip"
    override def infoText: String = "The number of iterative children to skip with double arrows"
    override def value: Var[Int] = SettingsViewController.getNumSkipIterativeChildren
}

case object NumSkipBreakpoints extends UserSetting[Int] {
    override def settingName: String = "Breakpoints to skip"
    override def infoText: String = "The number of breakpoints to skip forwards when in debug mode"
    override def value: Var[Int] = SettingsViewController.getNumSkipBreakpoints
}

case object ColorBlindMode extends UserSetting[Boolean] {
    override def settingName: String = "Colour Blind Mode"
    override def infoText: String = "Set Dill's theme to a more accessible viewing experience"
    override def value: Var[Boolean] = SettingsViewController.getColorBlindMode
}

val allUserSettings = List(
    NumSkipIterativeChildren,
    NumSkipBreakpoints,
    ColorBlindMode
)