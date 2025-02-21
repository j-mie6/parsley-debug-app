package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom

/**
  * Object containing functions and variables defining page state
  */
object AppStateController {
    /* Boolean representing the current page theme */
    val isLightMode: Var[Boolean] = Var(!dom.window.matchMedia("(prefers-color-scheme: dark)").matches)
    def toggleTheme(): Observer[Unit] = isLightMode.updater((old, unit) => !old)
    
    /* Toggle the current page theme */
    //TODO: check this is subscribed - think it must be bound?
    def updateDomTheme(theme: Signal[Boolean]) = {
        theme --> (theme => dom.document.documentElement.setAttribute("data-theme", if theme then "dark" else "light"))
    }
}
