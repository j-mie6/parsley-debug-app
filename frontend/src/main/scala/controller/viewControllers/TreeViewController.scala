package controller.viewControllers

import com.raquo.laminar.api.L.*

import model.DebugTree
import model.errors.DillException
import view.DebugTreeDisplay
import controller.tauri.Tauri
import controller.tauri.Command
import model.errors.DillException


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeViewController {

    /* Reactive DebugTree */
    private val tree: Var[Option[DebugTree]] = Var(None) 

    /* DebugTree signal */
    val getTree: Signal[Option[DebugTree]] = tree.signal

    /** Set debug tree */
    val setTree: Observer[DebugTree] = tree.someWriter

    /** Set optional debug tree (can be None) */
    val setTreeOpt: Observer[Option[DebugTree]] = tree.writer

    /** Set debug tree to None to stop rendering */
    def unloadTree: Observer[Unit] = Observer(_ => tree.set(None))
    
    /** Return true signal if tree is loaded into frontend */
    def treeExists: Signal[Boolean] = getTree.map(_.isDefined)

    /** Get debug tree element or warning if no tree found */
    def getTreeElem: Signal[HtmlElement] = getTree.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => div(
            className := "tree-view-error",
            "Nothing to show"
        )

        /* Render as DebugTreeDisplay */
        case Some(tree) => DebugTreeDisplay(tree)
    )


    /* Downloads tree to users device */
    def downloadTree(name: String): EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.DownloadTree, name)

    /* Imports JSON in path from users device */
    def importTree(name: String, contents: String): EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.ImportTree, (name, contents))
    
    /** Fetch the debug tree root from the backend, return in EventStream */
    def reloadTree: EventStream[Either[DillException, DebugTree]] = Tauri.invoke(Command.FetchDebugTree, ())
    
    /** Skips the current breakpoint 'skips' times
      *
      * @param skips The amount of times to skip a breakpoint
      */
    def skipBreakpoints(skips: Int): Unit =
        Tauri.invoke(Command.SkipBreakpoints, skips)
    
    /* Toggle whether the button to skip through breakpoints is visible */
    val isDebuggingSession: Signal[Boolean] = getTree.map(_.exists(_.isDebuggable))
}