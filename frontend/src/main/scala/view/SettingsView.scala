package view

import com.raquo.laminar.api.L.*

import model.settings.*
import model.toast.SettingsApplied
import model.toast.DefaultSettingsApplied
import controller.ToastController
import controller.viewControllers.SettingsViewController

private case class SettingView(setting: UserSetting) {

    def renderBool: Input = {
        input(
            typ := "checkbox",
            checked <-- SettingsViewController.getColorBlindMode.signal,
            onInput.mapToChecked --> setting.value.writer.contramap[Boolean](identity)
        )
    }

    def renderNumber: Input = {
        input(
            typ := "number",
            value <-- setting.value.signal.map(_.toString()),
            
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
    }
    
    def render: HtmlElement = {        
        div(
            className := "sidepanel-item-container",

            div(
                className := "sidepanel-item-name",
                setting.settingName,
            ),

            setting.style match {
                case "boolean" => renderBool
                case "number" => renderNumber
            }
        )
    }
}


object SettingsView {
    
    def apply(): HtmlElement = {
        div(
            className := "sidepanel-container settings",
            cls("open") <-- SettingsViewController.isSettingsOpen,

            div(className := "sidepanel-header", "Settings"),
            
            div(
                className := "sidepanel-items-container",
                allUserSettings.map(SettingView(_).render),
            ),

            div(flexGrow := 1),

            div(
                className := ("sidepanel-footer"),
                
                button("Apply", onClick --> {_ => 
                    /* Get all user setting values from local storage */
                    val appliedNumSkipIterativeChildren: Int =
                        allUserSettings
                            .find(_ == NumSkipIterativeChildren)
                            .flatMap(_.value.now() match {
                                case i: Int => Some(i) 
                                case _      => None
                            }).getOrElse(SettingsViewController.numSkipIterativeChildrenDefault)

                    val appliedNumSkipBreakpoints: Int =
                        allUserSettings
                            .find(_ == NumSkipBreakpoints)
                            .flatMap(_.value.now() match {
                                case i: Int => Some(i) 
                                case _      => None
                            }).getOrElse(SettingsViewController.numSkipBreakpointsDefault)

                    val appliedColorBlindMode: Boolean =
                        allUserSettings
                            .find(_ == ColorBlindMode)
                            .flatMap(_.value.now() match {
                                case b: Boolean => Some(b) 
                                case _      => None
                            }).getOrElse(SettingsViewController.colorBlindModeDefault)

                    /* Apply settings globally */
                    SettingsViewController.applySettings(
                        newNumSkipIterativeChildren = appliedNumSkipIterativeChildren,
                        newNumSkipBreakpoints = appliedNumSkipBreakpoints,
                        newColorBlindMode = appliedColorBlindMode
                    )

                    ToastController.setToast(SettingsApplied)
                }),

                button("Restore Defaults", onClick --> {_ =>
                    SettingsViewController.applySettings(
                        newNumSkipIterativeChildren = NumSkipIterativeChildren.default.asInstanceOf[Int],
                        newNumSkipBreakpoints = NumSkipBreakpoints.default.asInstanceOf[Int],
                        newColorBlindMode = ColorBlindMode.default.asInstanceOf[Boolean]
                    )

                    ToastController.setToast(DefaultSettingsApplied)
                })
            ),
        )
    }
}