package view

import com.raquo.laminar.api.L.*
import model.help.*
import controller.viewControllers.HelpViewController

object HelpView {
    def apply(activeHelpSection: Signal[HelpSection]): HtmlElement = {
        div(
            cls("help-popup-container"),
            hidden <-- HelpViewController.isPopupOpen,

            div(
                cls("help-popup"),
                
                div(cls("help-header"),
                    div(cls("help-close-button"), "✖", onClick --> (_ => HelpViewController.closePopup())),
                    h2("Help Guide")
                ),
                
                div(cls("help-content"),
                    h2(cls("help-title"), text <-- activeHelpSection.map(_.title)),
                    div(cls("help-section-text"), text <-- activeHelpSection.map (_.infoText)),
                    img(src <-- activeHelpSection.map(_.visualSource), cls("help-image"))
                ),
                
                div(cls("help-footer"),
                    child(button(cls("help-prev-button"), "Previous", onClick --> (_ => HelpViewController.prevSection()))) <-- HelpViewController.isFirstSection.not,
                    child(button(cls("help-next-button"), "Next", onClick --> (_ => HelpViewController.nextSection()))) <-- HelpViewController.isLastSection.not,
                    child(button(cls("help-done-button"), "Done", onClick --> (_ => HelpViewController.closePopup()))) <-- HelpViewController.isLastSection
                ),

                div(cls("help-progress-container"),
                    div(
                        cls("help-progress-fill"),
                        width <-- HelpViewController.progressPercentage.map(p => s"${p}%")
                    )
                )
            )
        )
    }
}