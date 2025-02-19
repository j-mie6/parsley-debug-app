package controller.viewControllers

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import upickle.default as up

import controller.DebugTreeController
import controller.errors.ErrorController
import controller.tauri.{Tauri, Command}
import controller.viewControllers.InputViewController

import view.DebugTreeDisplay


/**
* Object containing methods for manipulating the DebugTree.
*/
object TreeViewController {
    
    /* Display tree that will be rendered by TreeView */
    private val displayTree: Var[HtmlElement] = Var(div())

    /* Default tree view when no tree is loaded */
    private val noTreeFound: HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )
    
    /* Gets display tree element*/
    def getDisplayTree: HtmlElement = div(child <-- displayTree.signal)
    
    /**
    * Mutably updates the displayTree variable
    *
    * @param tree New element to update the displayTree variable
    */
    def setDisplayTree(tree: HtmlElement) = {
        displayTree.set(tree)
    }

    def setEmptyTree(): Unit = {
        setDisplayTree(noTreeFound)
    }
    
    /**
    * Fetch the debug tree root from the tauri backend.
    *
    * @param displayTree The var that the display tree HTML element will be written into.
    */
    def reloadTree(): Unit = {
        Tauri.invoke[String](Command.FetchDebugTree).onComplete {
            case Success(result) => DebugTreeController.decodeDebugTree(result) match {
                case Success(debugTree) => println(s"This worked!: result = $result"); setDisplayTree(DebugTreeDisplay(debugTree))
                case Failure(error) => println(s"Failed, but couldn't deserialise $error"); ErrorController.handleError(error)
            }
            case Failure(error) => println(s"Error: $error"); ErrorController.handleError(error)
        }
    }

    def saveTree(treeName: String): Unit = {
        Tauri.invoke[String](Command.SaveTree, Map("name" -> treeName)).onComplete {
            case Failure(error) => ErrorController.handleError(error)
            case Success(_) => ()
        }
    }

    def fetchSavedTreeNames(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String](Command.FetchSavedTreeNames).onComplete {
            case Success(names: String) => 
                /* Update fileNames with parsed names */
                fileNames.update(_ => up.read[List[String]](names))
            case Failure(error) => ErrorController.handleError(error)
        }
    }

    def loadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String](Command.LoadSavedTree, Map("name" -> treeName)).onComplete {
            case Success(trees) => trees.foreach { _ =>
                TreeViewController.reloadTree()
            }
            case Failure(error) => ErrorController.handleError(error)
        }
    }
        
}