package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import model.Page

import controller.InputController
import controller.tauri.{Tauri, Event}
import controller.TreeController

/**
  * Object containing rendering functions for the input view page.
  */

object InputView {
    def apply(): HtmlElement = div(InputController.getInput)
}