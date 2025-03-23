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
        /* Renders the input */
        div(className:= "debug-input-page", child.maybe <-- InputViewController.getInputElem) 
    }
}