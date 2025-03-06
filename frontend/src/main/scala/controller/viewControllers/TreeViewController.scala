package controller.viewControllers

import com.raquo.laminar.api.L.*

import model.DebugTree
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

    /** Set debug tree */
    val setTree: Observer[DebugTree] = tree.someWriter

    /** Set optional debug tree (can be None) */
    val setTreeOpt: Observer[Option[DebugTree]] = tree.writer

    /** Set debug tree to None to stop rendering */
    def unloadTree: Observer[Unit] = Observer(_ => tree.set(None))
    
    /** Return true signal if tree is loaded into frontend */
    def treeExists: Signal[Boolean] = tree.signal.map(_.isDefined)

    /** Get sessionId from loaded tree, -1 for non-debuggable */
    def getSessionId: Signal[Int] = tree.signal.map(_.get.sessionId)

    /** Get debug tree element or warning if no tree found */
    def getTreeElem: Signal[HtmlElement] = tree.signal.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => div(
            className := "tree-view-error",
            "Nothing to show"
        )

        /* Render as DebugTreeDisplay */
        case Some(tree) => DebugTreeDisplay(tree)
    )

    /** Fetch the debug tree root from the backend, return in EventStream */
    def reloadTree: EventStream[Either[DillException, DebugTree]] = Tauri.invoke(Command.FetchDebugTree, ())
    
    /** Skips the current breakpoint 'skips' times
      *
      * @param skips The amount of times to skip a breakpoint
      * @param sessionId The sessionId of the current debugging session
      */
    def skipBreakpoints(sessionId: Int, skips: Int): EventStream[Either[DillException, Unit]]
      = Tauri.invoke(Command.SkipBreakpoints, (sessionId, skips))
    
    /* Toggle whether the button to skip through breakpoints is visible */
    val isDebuggingSession: Signal[Boolean] = tree.signal.map(_.exists(_.sessionId != -1))
}