package controller

import com.raquo.laminar.api.L.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

import upickle.default as up
import org.scalablytyped.runtime.StringDictionary

import view.DebugTreeDisplay


/**
* Object containing methods for manipulating the DebugTree.
*/
object TreeController {
    
    /* Display tree that will be rendered by TreeView */
    private val displayTree: Var[HtmlElement] = Var(div())
    
    /* Gets display tree element*/
    def getDisplayTree: Var[HtmlElement] = displayTree
    
    def renderDisplayTree = displayTree.signal
    
    /**
    * Mutably updates the displayTree variable
    *
    * @param tree New element to update the displayTree variable
    */
    def setDisplayTree(tree: HtmlElement) = {
        displayTree.set(tree)
    }
    
    /**
    * Fetch the debug tree root from the tauri backend.
    *
    * @param displayTree The var that the display tree HTML element will be written into.
    */
    def reloadTree(): Unit = {
        for {
            treeString <- Tauri.invoke[String]("fetch_debug_tree")
        } do {
            setDisplayTree(
                if treeString.isEmpty then div("No tree found") else 
                DebugTreeHandler.decodeDebugTree(treeString) match {
                    case Failure(exception) => 
                        println(s"Error in decoding debug tree : ${exception.getMessage()}");
                        div()
                    case Success(debugTree) => DebugTreeDisplay(debugTree)
                }
            )
        }   
    } 
}