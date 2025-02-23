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

    /* Set debug tree */
    def setTree: Observer[DebugTree] = tree.someWriter

    /* Set debug tree (can be None) */
    def setTreeOpt: Observer[Option[DebugTree]] = tree.writer
    
    def treeExists: Signal[Boolean] = tree.signal.map(_.isDefined)

    /* Get debug tree element */
    def getTreeElem: Signal[HtmlElement] = tree.signal.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => div(
            className := "tree-view-error",
            "No tree found! Start debugging by attaching DillRemoteView to a parser"
        )

        /* Render as DebugTreeDisplay */
        case Some(tree) => DebugTreeDisplay(tree)
    )

    
    /* Fetch the debug tree root from the backend, return in EventStream */
    def reloadTree: EventStream[DebugTree] = Tauri.invoke(Command.FetchDebugTree, ()).collectRight
    
}