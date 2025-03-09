package controller.viewControllers

import com.raquo.laminar.api.L.*

import model.DebugTree
import model.errors.DillException
import model.errors.DillException
import view.DebugTreeDisplay
import controller.tauri.Tauri
import controller.tauri.Command
import controller.viewControllers.SettingsViewController



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

    // /** Set debug tree */
    // val setTree2: Observer[Option[DebugTree]] = Observer(newTree => newTree.get.sessionId)

    /** Set optional debug tree (can be None) */
    val setTreeOpt: Observer[Option[DebugTree]] = tree.writer

    /** Set debug tree to None to stop rendering */
    def unloadTree: Observer[Unit] = Observer(_ => tree.set(None))
    
    /** Return true signal if tree is loaded into frontend */
    def treeExists: Signal[Boolean] = getTree.map(_.isDefined)

    val loadedTrees: Var[Map[Int, DebugTree]] = Var(Map.empty)

    val getLoadedTrees: Signal[Map[Int, DebugTree]] = loadedTrees.signal
    val setLoadedTrees: Observer[Map[Int, DebugTree]] = loadedTrees.writer

    // val reloadTree: Observer[Int] = Observer(sessionId => )

    val test: Observer[Int] = Observer(sessionId => loadedTrees.set(loadedTrees.now().updated(sessionId, tree.now().get)))


    /** Get sessionId from loaded tree, -1 for non-debuggable */
    def getSessionId: Signal[Int] = tree.signal.map(_.get.sessionId)

    /** Get debug tree element or warning if no tree found */
    def getTreeElem: Signal[HtmlElement] = getTree.map(_ match 
        /* Default tree view when no tree is loaded */
        case None => 
          StateManagementViewController.clearRefs()
          div(
            className := "nothing-shown",
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
      * @param sessionId The sessionId of the current debugging session
      */
    def skipBreakpoints(sessionId: EventStream[Int]): EventStream[Either[DillException, Unit]] = {
        sessionId
            .withCurrentValueOf(SettingsViewController.getNumSkipBreakpoints.signal, StateManagementViewController.getRefs)
            .map((sessionId, skips, refs) => (sessionId, skips - 1, refs))
            .flatMapMerge(Tauri.invoke(Command.SkipBreakpoints, _))
    }
        
    val isDebuggingSession: Signal[Boolean] = tree.signal.map(_.exists(_.isDebuggable))
}