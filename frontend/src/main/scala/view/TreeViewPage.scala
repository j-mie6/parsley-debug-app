package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.TreeController

/**
  * Object containing rendering functions for the TreeViewPage.
  */
object TreeViewPage extends DebugViewPage {

    def apply(): HtmlElement = {        
        super.render(Some(div(
            TreeController.getDisplayTree, /* Renders tree */
        )))
    }
}
