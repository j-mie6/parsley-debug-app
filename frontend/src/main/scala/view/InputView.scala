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
import model.DebugNode


/**
  * Object containing rendering functions for the input view page.
  */
object InputView {
    def apply(): HtmlElement = {
        div(className := "debug-input-page", child <-- InputViewController.getInputElem) 
    }


    /** Render a unselected input */ 
    def renderInput(input: String): HtmlElement = {
        div(
            className := "debug-input-container", 
            renderInputString(input)
        )
    }

    /** Render a unselected input */ 
    def renderInput(input: String, selected: DebugNode): HtmlElement = {
        div(
            className:= "debug-input-container", 

            /* Input before selected input */
            renderInputString(input.slice(0, selected.inputStart)),

            /* Selected input */
            renderSelectedInput(input, selected),

            /* Input after selected input */
            renderInputString(input.slice(selected.inputEnd, input.length()))
        )
    }

    /** Render a single contiguous unselected input string */ 
    private def renderInputString(input: String): HtmlElement = {
        span(child(span(className := "debug-input", input)) := input.nonEmpty)
    }

    /** Render the selected input string */ 
    private def renderSelectedInput(fullInput: String, selected: DebugNode): HtmlElement = {
        /* If nothing consumed by node, augments are equal */
        val empty: Boolean = selected.inputStart == selected.inputEnd;
        
        span(
            className := "debug-input selected",
            
            cls("empty") := empty,
            cls("fail") := !selected.success,
            cls("iterative") := selected.isIterative,
            cls("debug") := selected.isBreakpoint,

            if (empty)
                i(
                    className := "bi",
                    cls("bi-exclamation-lg") := !selected.success,
                    cls("bi-pause-fill") := selected.isBreakpoint
                )
            else
                fullInput.slice(selected.inputStart, selected.inputEnd)
        )
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