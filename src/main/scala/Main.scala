import org.scalajs.dom

import com.raquo.laminar.api.L._


  @main def hello = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    appElement()
  )

  def appElement(): Element = h1("Helelo")