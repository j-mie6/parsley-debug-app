import org.scalajs.dom

import com.raquo.laminar.api.L.*

import Display.DisplayTree

  @main def hello(): Unit = renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    appElement()
  )

  def appElement(): Element = DisplayTree.SAMPLE_TREE.element