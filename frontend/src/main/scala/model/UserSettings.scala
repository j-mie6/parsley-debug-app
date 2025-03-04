package model.settings

import com.raquo.laminar.api.L.*

import controller.viewControllers.SettingsViewController

/**
 * Defines the possible data types for a user setting.
 *
 * A user setting can store either an `Int` (for numerical settings) 
 * or a `Boolean` (for toggleable options). This type alias ensures
 * type safety when handling different kinds of settings within `Dill`.
 */
type UserSettingType = Int | Boolean

/**
 * Represents a user-configurable setting within the application.
 *
 * The UserSetting trait defines the structure for a setting, including its name,
 * descriptive information, and a temporary storage variable (`value`) before
 * applying the setting globally. Each concrete implementation of `UserSetting`
 * specifies a unique type of setting, such as numerical values or boolean flags.
 *
 * @param settingName The display name of the setting.
 * @param infoText A brief description explaining the purpose of the setting.
 * @param style A string identifier specifying the type of input (e.g., "number", "boolean").
 * @param value A reactive variable (`Var`) that temporarily stores the user's selected value
 *              before it is applied globally. The type of this variable is defined
 *              by `UserSettingType`.
 */
sealed trait UserSetting {
    def settingName: String
    def infoText: String
    def style: String

    /* Used as a temporary store of the setting prior to applying it platform-wide */
    def value: Var[UserSettingType]
}

case object NumSkipIterativeChildren extends UserSetting {
    override def settingName: String = "Iterative children to skip"
    override def infoText: String = "The number of iterative children to skip with double arrows"
    override def style: String = "number"
    override def value: Var[UserSettingType] = SettingsViewController.getNumSkipIterativeChildren.asInstanceOf[Var[UserSettingType]]
}

case object NumSkipBreakpoints extends UserSetting {
    override def settingName: String = "Breakpoints to skip"
    override def infoText: String = "The number of breakpoints to skip forwards when in debug mode"
    override def style: String = "number"

    override def value: Var[UserSettingType] = SettingsViewController.getNumSkipBreakpoints.asInstanceOf[Var[UserSettingType]]
}

case object ColorBlindMode extends UserSetting {
    override def settingName: String = "Colour Blind Mode"
    override def infoText: String = "Set Dill's theme to a more accessible viewing experience"
    override def style: String = "boolean"

    override def value: Var[UserSettingType] = SettingsViewController.getColorBlindMode.asInstanceOf[Var[UserSettingType]]
}

/**
 * A list of all available user settings in the application.
 *
 * This list contains all configurable settings, such as numerical values 
 * (e.g., the number of iterative children to skip) and boolean options 
 * (e.g., enabling colour-blind mode). It serves as a centralised collection 
 * for managing user preferences.
 */
val allUserSettings = List(
    NumSkipIterativeChildren,
    NumSkipBreakpoints,
    ColorBlindMode
)