package view

import com.raquo.laminar.api.L.*
import model.settings.*
import controller.viewControllers.SettingsViewController

object SettingsView {

    val settingsModified: Var[Boolean] = Var(true)

    def renderUserSetting(setting: UserSetting): HtmlElement = {
        val titleHoverVar: Var[Boolean] = Var(false)

        div(
            className := "single-setting-container",
            div(
                div (
                    className := "single-setting-top-container",
                    div(
                        className := "single-setting-name-container",
                        cls("underlined") <-- titleHoverVar,
    
                        onMouseOver.mapTo(true) --> titleHoverVar,
                        onMouseOut.mapTo(false) --> titleHoverVar,

                        setting.settingName,
                    ),
                    div(
                        className := "single-setting-modify-container",
                        setting.style match
                            case "number" =>
                                input(
                                    /* Would be nice to set this reactively but I guess it doesn't matter? */
                                    defaultValue := setting.value.now().toString(),
                                    typ := "number",
                                    minAttr := "0",
                                    /* If user has entered a non-int value into the input then do not change the setting */
                                    onInput.mapToValue.map(_.toIntOption.getOrElse(setting.value.now())) --> setting.value.writer
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
                child(
                    div(
                        className := "single-setting-info-text",
                        setting.infoText
                    )
                ) <-- titleHoverVar,
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
                    allUserSettings.map((thisSetting: UserSetting ) => renderUserSetting(thisSetting))
                ),

                div(
                    cls("settings-footer"),
                    child(button(className := "apply-settings-button", "Apply", onClick --> (_ => 
                        SettingsViewController.applySettings(
                            newNumSkipIterativeChildren = 
                                allUserSettings
                                .find(_ == NumSkipIterativeChildren)
                                .flatMap(_.value.now() match {
                                    case i: Int => Some(i) 
                                    case _      => None
                                }).getOrElse(5),
                            newNumSkipBreakpoints =
                                allUserSettings
                                .find(_ == NumSkipBreakpoints)
                                .flatMap(_.value.now() match {
                                    case i: Int => Some(i) 
                                    case _      => None
                                }).getOrElse(1),
                            newColorBlindMode = 
                                allUserSettings
                                .find(_ == ColorBlindMode)
                                .flatMap(_.value.now() match {
                                    case b: Boolean => Some(b) 
                                    case _      => None
                                }).getOrElse(false),
                        )))) <-- settingsModified,
                ),
            )
        )
    }
}