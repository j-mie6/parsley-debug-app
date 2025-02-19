package view

import com.raquo.laminar.codecs.*
import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import model.Page

import controller.tauri.{Tauri, Event}
import controller.viewControllers.InputViewController
import controller.viewControllers.TreeViewController

/**
  * Object containing rendering functions for the input view page.
  */

object InputView {

   /**
     * Converts an input string to an HTML element
     * 
     * @param input The input string
     * @return An HTML element displaying the input string
     */
    def toInputElement(input: Signal[String]): HtmlElement = h1(
                className := "debug-tree-title",
                p("Parser Input : ", margin.px := 0, fontSize.px := 15,
                    fontStyle.italic, fontWeight.lighter),
                text <-- input
    )
    

    def apply(): HtmlElement = div(toInputElement(InputViewController.getInput.signal))
}