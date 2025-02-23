package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom

/**
  * Object containing functions and variables defining page state
  */
object AppStateController {
    
    /* Reactive variable representing the current page theme */
    private val theme: Var[Boolean] = Var(!dom.window.matchMedia("(prefers-color-scheme: dark)").matches)

    /** Get signal indicating whether app is in light or dark mode */
    def isLightMode: Signal[Boolean] = theme.signal

    /** Toggle theme between light and dark */
    def toggleTheme(): Observer[Unit] = theme.updater((old: Boolean, unit: Unit) => !old)
    
    /** Set theme as DOM CSS page attribute */
    def updateDomTheme(): Observer[Boolean] = {
        Observer(
            (isLightTheme: Boolean) => dom.document
                .documentElement
                .setAttribute("data-theme", if isLightTheme then "dark" else "light")
        )
    }

    /* Icons for showing page theme */
    private lazy val sunIcon: HtmlElement = i(className := "bi bi-brightness-high-fill", fontSize.px := 33)
    private lazy val moonIcon: HtmlElement = i(className := "bi bi-moon-fill", fontSize.px := 33)

    /** Get boolean signal indicating whether light theme is active */
    def getThemeIcon: Signal[HtmlElement] = theme.signal
        .map(if (_) then moonIcon else sunIcon)

}
