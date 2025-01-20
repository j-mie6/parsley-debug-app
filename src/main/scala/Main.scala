import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global

import Display.DisplayTree

import com.raquo.laminar.api.L._

import lib.Tauri
import pages.DebugViewPage

@main def hello = renderOnDomContentLoaded(
  dom.document.getElementById("app"),
  // DebugViewPage()
  div()
)
