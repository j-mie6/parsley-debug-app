package view

import com.raquo.laminar.api.L.*

import org.scalajs.dom

import scala.util.Failure
import scala.util.Success

import model.DebugTree

import controller.TreeViewController

/**
  * Object containing rendering functions for the TreeView
  */
object TreeView {
    def apply(): HtmlElement = div(
            TreeViewController.getDisplayTree, /* Renders tree */
    )
}
