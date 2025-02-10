package controller

import com.raquo.laminar.api.L.*
import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import view.DebugTreeDisplay
import org.scalablytyped.runtime.StringDictionary

import upickle.default as up

/**
  * TreeController provides interface for updating DebugTree
  */
object TreeController {
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

    def saveTree(treeName: String): Unit = Tauri.invoke[String]("save_debug_tree", Map("name" -> treeName))

    def getTrees(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String]("get_saved_trees").foreach { serializedNames =>
            // Update fileNames with parsed names
            fileNames.update(_ => up.read[List[String]](serializedNames))
        }
    }

    def reloadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String]("reload_tree", Map("name" -> treeName)).foreach { _ =>
            TreeController.reloadTree(displayTree)
        }
    }
        

}