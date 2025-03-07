package view

import scala.util.Failure
import scala.util.Success

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import model.DebugTree
import controller.viewControllers.TreeViewController
import controller.errors.ErrorController

/**
  * Object containing rendering functions for the TreeView
  */
object TreeView {

    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(
        child <-- TreeViewController.getTreeElem, /* Renders the tree */
    )
            
}
