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

    /**
      * Converts a tree to an HTML element
      *
      * @return An HTML element displaying the tree
      */
    def apply(): HtmlElement = {
        div(
            TreeViewController.getDisplayTree   /* Renders the tree */
        )
    }
}
