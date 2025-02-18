package controller

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import upickle.default as up

import controller.InputViewController
import controller.tauri.{Tauri, Command}

import view.DebugTreeDisplay


/**
* Object containing methods for manipulating the DebugTree.
*/
object TreeController {
    
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
        for {
            treeString <- Tauri.invoke[String](Command.FetchDebugTree)
        } do {
            setDisplayTree(
                if treeString.isEmpty then div("No tree found") else 
                DebugTreeHandler.decodeDebugTree(treeString) match {
                    case Failure(exception) => 
                        println(s"Error in decoding debug tree : ${exception.getMessage()}");
                        div()
                    case Success(debugTree) => {
                        InputViewController.setInput(debugTree.input)
                        DebugTreeDisplay(debugTree)
                    }
                }
            )
        }
    }

    def saveTree(treeName: String): Unit = Tauri.invoke[String](Command.SaveTree, Map("name" -> treeName))

    def fetchSavedTreeNames(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String](Command.FetchSavedTreeNames).foreach { serializedNames =>
            // Update fileNames with parsed names
            fileNames.update(_ => up.read[List[String]](serializedNames))
        }
    }

    def loadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String](Command.LoadSavedTree, Map("name" -> treeName)).foreach { _ =>
            TreeController.reloadTree()
        }
    }
        
}