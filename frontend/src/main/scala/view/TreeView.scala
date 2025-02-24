package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.viewControllers.TreeViewController

/**
  * Object containing rendering functions for the TreeView
  */
object TreeView {

    /* Fast forward icon for skipping */
    private lazy val skipIcon: Element = i(className := "bi bi-fast-forward-fill", fontSize.px := 33)

    /* Adds ability to skip the current breakpoint. */
    private lazy val skipButton: Element = button(
        className := "skip-button",

        skipIcon, /* Fast forward icon */

        onClick --> { _ => {
            TreeViewController.skipBreakpoints(0)
        }}
    )


    /**
      * Converts a tree to an HTML element
      *
      * @return An HTML element displaying the tree
      */
    def apply(): HtmlElement = div(
        skipButton,

        TreeViewController.getDisplayTree, /* Renders the tree */
    )
}
