package view

import com.raquo.laminar.api.L.*

import model.settings.*
import model.toast.SettingsApplied
import model.toast.DefaultSettingsApplied
import controller.ToastController
import controller.viewControllers.SettingsViewController

object SettingsView {

    def renderUserSetting(setting: UserSetting): HtmlElement = {
        val titleHoverVar: Var[Boolean] = Var(false)
        
        div(
            className := "single-setting-container",
            div(
                div (
                    className := "single-setting-top-container",
                    div(
                        className := "single-setting-name-container",

                        setting.settingName,
                    ),
                    div(
                        className := "single-setting-modify-container",
                        setting.style match
                            case "number" =>
                                input(
                                    value <-- setting.value.signal.map(_.toString()),
                                    typ := "number",
                                    minAttr := "0",
                                    /* Update setting only when a valid number is entered */
                                    onInput.mapToValue --> { newValue =>
                                        if (newValue.isEmpty) {
                                            /* Allow empty temporarily */
                                            setting.value.set(false) 
                                        } else {
                                            newValue.toIntOption.foreach(setting.value.set)
                                        }
                                    },

                                    /* Restore default value if empty when input loses focus */
                                    onBlur(_
                                        .sample(setting.value.signal)
                                        .filter((b: UserSettingType) => b == false)
                                        .mapTo(setting.default)
                                    ) --> setting.value.writer
                                )
                            case "boolean" =>
                                input(
                                    typ := "checkbox",
                                    checked <-- SettingsViewController.getColorBlindMode.signal,
                                    onInput.mapToChecked --> setting.value.writer.contramap[Boolean](b => b)
                                )
                        )
                    )
                ),
            )
    }   


    def apply(): HtmlElement = {
        div(
            className := "settings-sidepanel-container",
            cls("open") <-- SettingsViewController.isSettingsOpen,

            div(
                className := ("settings-sidepanel"),
                
                div(cls("settings-header"),
                    h2("Settings")
                ),
                
                div(
                    cls("settings-content"),
                    allUserSettings.map(renderUserSetting(_))
                ),

                div(
                    cls("settings-footer"),
                    button(className := "apply-settings-button", "Apply", onClick --> (_ => 
                        /* Get all user setting values from local storage */
                        val appliedNumSkipIterativeChildren: Int =
                            allUserSettings
                                .find(_ == NumSkipIterativeChildren)
                                .flatMap(_.value.now() match {
                                    case i: Int => Some(i) 
                                    case _      => None
                                }).getOrElse(5)

                        val appliedNumSkipBreakpoints: Int =
                            allUserSettings
                                .find(_ == NumSkipBreakpoints)
                                .flatMap(_.value.now() match {
                                    case i: Int => Some(i) 
                                    case _      => None
                                }).getOrElse(1)

                        val appliedColorBlindMode: Boolean =
                            allUserSettings
                                .find(_ == ColorBlindMode)
                                .flatMap(_.value.now() match {
                                    case b: Boolean => Some(b) 
                                    case _          => None
                                }).getOrElse(false)

                        /* Apply settings globally */
                        SettingsViewController.applySettings(
                            newNumSkipIterativeChildren = appliedNumSkipIterativeChildren,
                            newNumSkipBreakpoints = appliedNumSkipBreakpoints,
                            newColorBlindMode = appliedColorBlindMode
                        )

                        ToastController.setToast(SettingsApplied)
                    )),
                    button(className:= "default-settings-button", "Restore Defaults", onClick --> {_ =>
                        SettingsViewController.applySettings(
                            newNumSkipIterativeChildren = NumSkipIterativeChildren.default.asInstanceOf[Int],
                            newNumSkipBreakpoints = NumSkipBreakpoints.default.asInstanceOf[Int],
                            newColorBlindMode = ColorBlindMode.default.asInstanceOf[Boolean]
                        )

                        ToastController.setToast(DefaultSettingsApplied)

                    })
                ),
            )
        )
    }
}