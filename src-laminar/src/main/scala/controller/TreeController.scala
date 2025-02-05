package controller

import com.raquo.laminar.api.L.*
import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import view.DebugTreeDisplay
import org.scalablytyped.runtime.StringDictionary

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param displayTree Display tree HTML element
  */
object TreeController {
    def reloadTree(displayTree: Var[HtmlElement]): Unit = 
        for {
            // treeString <- Tauri.invoke[String]("fetch_node_children", StringDictionary(("nodeId", 0)))
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