package controller

import com.raquo.laminar.api.L.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import upickle.default as up
import org.scalablytyped.runtime.StringDictionary

import view.DebugTreeDisplay
import view.error.ErrorHandler

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
        Tauri.invoke[String]("fetch_debug_tree").onComplete {
            case Success(result) => DebugTreeHandler.decodeDebugTree(result) match {
                case Success(debugTree) => println(s"This worked!: result = $result"); displayTree.set(DebugTreeDisplay(debugTree))
                case Failure(error) => println(s"Failed, but couldn't deserialise $error"); ErrorHandler.handleError(error)
            }
            case Failure(error) => println(s"Error: $error"); ErrorHandler.handleError(error)
        }
        

    def saveTree(treeName: String): Unit = 
        Tauri.invoke[String]("save_tree", Map("name" -> treeName)).onComplete {
            case Failure(error) => ErrorHandler.handleError(error)
            case Success(_) => ()
        }

    def fetchSavedTreeNames(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String]("fetch_saved_tree_names").onComplete {
            case Success(names: String) => 
                // Update fileNames with parsed names
                fileNames.update(_ => up.read[List[String]](names))
            case Failure(error) => ErrorHandler.handleError(error)
        }
        
    }

    def loadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String]("load_saved_tree", Map("name" -> treeName)).onComplete {
            case Success(trees) => trees.foreach { _ =>
                TreeController.reloadTree(displayTree)
            }
            case Failure(error) => ErrorHandler.handleError(error)
        }
    }
        

}