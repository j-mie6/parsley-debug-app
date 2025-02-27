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

    /* DebugTree signal */
    val treeSignal: Signal[Option[DebugTree]] = tree.signal

    /** Set debug tree */
    val setTree: Observer[DebugTree] = tree.someWriter

    /** Set optional debug tree (can be None) */
    val setTreeOpt: Observer[Option[DebugTree]] = tree.writer

    /** Set debug tree to None to stop rendering */
    def unloadTree: Observer[Unit] = Observer(_ => tree.set(None))
    
    /** Return true signal if tree is loaded into frontend */
    def treeExists: Signal[Boolean] = tree.signal.map(_.isDefined)

    /** Get debug tree element or warning if no tree found */
    def getTreeElem: Signal[HtmlElement] = tree.signal.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => div(
            className := "tree-view-error",
            "No tree found! Start debugging by attaching DillRemoteView to a parser"
        )

        /* Render as DebugTreeDisplay */
        case Some(tree) => DebugTreeDisplay(tree)
    )

    /** Fetch the debug tree root from the backend, return in EventStream */
<<<<<<< Updated upstream
    def reloadTree: EventStream[DebugTree] = Tauri.invoke(Command.FetchDebugTree, ()).collectRight
=======
    def reloadTree: EventStream[Either[DillException, DebugTree]] = Tauri.invoke(Command.FetchDebugTree, ())

    /* Downloads tree to users device */
    def downloadTree: EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.DownloadTree, ())
>>>>>>> Stashed changes
    
}