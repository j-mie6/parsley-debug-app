package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom

/**
  * Object containing functions and variables defining page state
  */
object AppStateController {
    /* Boolean representing the current page theme */
    val isLightMode: Var[Boolean] = Var(!dom.window.matchMedia("(prefers-color-scheme: dark)").matches)

    /* Toggle the current page theme */
    def toggleTheme() = {
        dom.document.documentElement.setAttribute("data-theme", if isLightMode.now() then "dark" else "light");
        isLightMode.set(!isLightMode.now())
    }
}
