package view

import scala.util.Failure
import scala.util.Success

import org.scalajs.dom
import com.raquo.laminar.api.L.*

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
    def apply(): HtmlElement = div(
        child <-- TreeViewController.displayTree, /* Renders the tree */
    )
            
}
