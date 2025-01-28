package lib

import com.raquo.laminar.api.L.*

import org.scalajs.dom

object State {

  val isLightMode: Var[Boolean] = Var(!dom.window.matchMedia("(prefers-color-scheme: dark)").matches)

  def toggleTheme() = {
    dom.document.documentElement.setAttribute("data-theme", if isLightMode.now() then "dark" else "light");
    isLightMode.set(!isLightMode.now())
  }
}
