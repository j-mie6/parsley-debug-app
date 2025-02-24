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

    /* Render tree as HtmlElement */
    def apply(): HtmlElement = div(
        child <-- TreeViewController.getTreeElem, /* Renders the tree */
    )
            
}
