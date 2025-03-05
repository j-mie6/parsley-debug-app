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
    def default: UserSettingType

    /* Used as a temporary store of the setting prior to applying it platform-wide */
    def value: Var[UserSettingType]
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

/**
 * Updates all user settings to the provided new values.
 *
 * This function applies changes to the global settings by updating the respective reactive variables.
 * The changes take effect immediately and can be observed in the user interface.
 *
 * @param newNumSkipIterativeChildren The new number of iterative children to skip.
 * @param newNumSkipBreakpoints The new number of breakpoints to skip.
 * @param newColorBlindMode The new value for enabling or disabling colour blind mode.
 */
def updateAllUserSettings(newNumSkipIterativeChildren: Int, newNumSkipBreakpoints: Int, newColorBlindMode: Boolean): Unit = {
    NumSkipIterativeChildren.value.set(newNumSkipIterativeChildren)
    NumSkipBreakpoints.value.set(newNumSkipBreakpoints)
    ColorBlindMode.value.set(newColorBlindMode)

}

/**
 * A user setting that determines the number of iterative children to be skipped.
 *
 * This setting controls how many iterative children are skipped when using the double arrow navigation.
 * It is stored as an integer and can be modified by the user.
 */
case object NumSkipIterativeChildren extends UserSetting {
    override def settingName: String = "Iterative children to skip"

    override def infoText: String = "The number of iterative children to skip with double arrows"

    override def style: String = "number"

    override def default: UserSettingType = 5

    override def value: Var[UserSettingType] = SettingsViewController.getNumSkipIterativeChildren.asInstanceOf[Var[UserSettingType]]
}

/**
 * A user setting that determines the number of breakpoints to be skipped.
 *
 * When debugging, this setting defines how many breakpoints are skipped forwards when stepping through execution.
 * It is stored as an integer and can be adjusted by the user.
 */
case object NumSkipBreakpoints extends UserSetting {
    override def settingName: String = "Breakpoints to skip"

    override def infoText: String = "The number of breakpoints to skip forwards when in debug mode"

    override def style: String = "number"

    override def default: UserSettingType = 1

    override def value: Var[UserSettingType] = SettingsViewController.getNumSkipBreakpoints.asInstanceOf[Var[UserSettingType]]
}

/**
 * A user setting that enables or disables colour blind mode.
 *
 * When activated, this setting adjusts the theme of the application to provide a more accessible viewing experience
 * for users with colour vision deficiencies.
 */
case object ColorBlindMode extends UserSetting {
    override def settingName: String = "Colour Blind Mode"

    override def infoText: String = "Set Dill's theme to a more accessible viewing experience"

    override def style: String = "boolean"

    override def default: UserSettingType = false

    override def value: Var[UserSettingType] = SettingsViewController.getColorBlindMode.asInstanceOf[Var[UserSettingType]]
}
