package controller

import com.raquo.laminar.api.L.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import view.DebugTreeDisplay

/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeController {
  /**
    * Fetch the debug tree root from the tauri backend.
    *
    * @param displayTree The var that the display tree HTML element will be written into.
    */
  def reloadTree(displayTree: Var[HtmlElement]): Unit = 
    for {
      treeString <- Tauri.invoke[String]("fetch_debug_tree")
    } do {
      displayTree.set(
        if treeString.isEmpty then div("No tree found") else 
          DebugTreeHandler.decodeDebugTree(treeString) match {
            case Failure(exception) => println(s"Error in decoding debug tree : ${exception.getMessage()}"); div()
            case Success(debugTree) => DebugTreeDisplay(debugTree)
          }
      )
    }
}