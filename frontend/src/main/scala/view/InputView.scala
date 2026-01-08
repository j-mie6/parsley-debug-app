package view

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.Page
import controller.viewControllers.InputViewController
import controller.viewControllers.TreeViewController
import controller.tauri.{Tauri, Event}


/**
  * Object containing rendering functions for the input view page.
  */
object InputView {
    def apply(): HtmlElement = {
        div(className:= "debug-input-page", child <-- InputViewController.getInputElem) 
    }
}

object InputViewSidepanel {
        def apply(): HtmlElement = {
        div(
            className := "sidepanel-container state",
            cls("open") <-- InputViewController.isInputOpen,

            div(className := "sidepanel-header", "Input"),

            /* Renders the input */
            child <-- InputViewController.getInputElem
        )
    }
}