package controller.viewControllers

import com.raquo.laminar.api.L.*

import model.DebugTree
import view.DebugTreeDisplay
import controller.tauri.Tauri
import controller.tauri.Command


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeViewController {

    /* Reactive DebugTree */
    private val tree: Var[Option[DebugTree]] = Var(None) 

    /* Helpers for writing the tree */
    def setTree(): Observer[DebugTree] = tree.someWriter
    def setTreeOpt(): Observer[Option[DebugTree]] = tree.writer
    
    
    /**
    * Fetch the debug tree root from the tauri backend.
    *
    * @param displayTree The var that the display tree HTML element will be written into.
    */
    def reloadTree(): EventStream[DebugTree] = Tauri.invoke(Command.FetchDebugTree, ()).collectRight
    
    
    /* Default tree view when no tree is loaded */
    private val noTreeFound: HtmlElement = div(
        className := "tree-view-error",
        "No tree found! Start debugging by attaching DillRemoteView to a parser"
    )

    /**
     * Gets display tree element
     */
    def getDisplayTree: Signal[HtmlElement] = tree.signal.map(_ match 
        case None => noTreeFound
        case Some(tree) => DebugTreeDisplay(tree)
    ) 
    
}