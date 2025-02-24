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
    private val displayTree: Var[HtmlElement] = Var(MainViewController.getNoTreeFound)
    
    /**
    * Gets display tree element
    */
    def getDisplayTree: HtmlElement = div(child <-- displayTree.signal)
    
    /**
    * Mutably updates the displayTree variable
    *
    * @param tree New element to update the displayTree variable
    */
    def setDisplayTree(tree: HtmlElement) = displayTree.set(tree)

    /**
    * Sets the display tree to the default noTreeFound element
    */
    def setEmptyTree(): Unit = setDisplayTree(MainViewController.getNoTreeFound)
    
    /**
    * Fetch the debug tree root from the tauri backend.
    *
    * @param displayTree The var that the display tree HTML element will be written into.
    */
    def reloadTree(): Unit = {
        Tauri.invoke[String](Command.FetchDebugTree).onComplete {
            case Success(result) => DebugTreeController.decodeDebugTree(result) match {
                case Success(debugTree) => {
                    println(s"This worked!: result = $result")
                    InputViewController.toInputElement(debugTree.input)
                    setDisplayTree(DebugTreeDisplay(debugTree))
                } 
                
                case Failure(error) => println(s"Failed, but couldn't deserialise $error"); ErrorController.handleException(error)
            }
            case Failure(error) => println(s"Error: $error"); ErrorController.handleException(error)
        }
    }

    /**
    * Saves the current tree to a file
    *
    * @param treeName The name of the tree to save
    */
    def saveTree(treeName: String): Unit = {
        Tauri.invoke[String](Command.SaveTree, Map("name" -> treeName)).onComplete {
            case Success(_) => ()
            case Failure(error) => ErrorController.handleException(error)
        }
    }

    /**
      * Fetches all tree names saved by the user from the backend
      *
      * @param fileNames List of all trees saved by the user
      */
    def fetchSavedTreeNames(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String](Command.FetchSavedTreeNames).onComplete {
            case Success(names: String) => 
                /* Update fileNames with parsed names */
                fileNames.update(_ => up.read[List[String]](names))
            case Failure(error) => ErrorController.handleException(error)
        }
    }

    /**
      * Loads a saved tree from the backend into the display tree
      *
      * @param treeName User-defined name of the tree to be loaded
      * @param displayTree Tree element to load and display in a given tree
      */
    def loadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String](Command.LoadSavedTree, Map("name" -> treeName)).onComplete {
            case Success(trees) => trees.foreach { _ =>
                TreeViewController.reloadTree()
            }
            case Failure(error) => ErrorController.handleException(error)
        }
    }
        
}