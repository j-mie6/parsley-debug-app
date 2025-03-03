package model.settings

import com.raquo.laminar.api.L.*

import controller.viewControllers.SettingsViewController

type SettingVar = Var[Int] | Var[Boolean]

sealed trait UserSetting {
    def settingName: String
    def infoText: String
    def settingVar: SettingVar
}

case object NumSkipIterativeChildren extends UserSetting {
    override def settingName: String = "Iterative children to skip"
    override def infoText: String = "The number of iterative children to skip with double arrows"
    override def settingVar: SettingVar = SettingsViewController.getNumSkipIterativeChildren
}

case object NumSkipBreakpoints extends UserSetting {
    override def settingName: String = "Breakpoints to skip"
    override def infoText: String = "The number of breakpoints to skip forwards when in debug mode"
    override def settingVar: SettingVar = SettingsViewController.getNumSkipBreakpoints
}

case object ColorBlindMode extends UserSetting {
    override def settingName: String = "Colour Blind Mode"
    override def infoText: String = "Set Dill's theme to a more accessible viewing experience"
    override def settingVar: SettingVar = SettingsViewController.getColorBlindMode
}

val allUserSettings: List[UserSetting] = List(
    NumSkipIterativeChildren,
    NumSkipBreakpoints,
    ColorBlindMode
)