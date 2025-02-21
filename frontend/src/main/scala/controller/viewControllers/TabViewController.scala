package controller.viewControllers

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import upickle.default as up

import controller.tauri.{Tauri, Command}
import controller.viewControllers.TreeViewController
import model.DebugTree


/**
* TabViewController object contains functions that modify the state of 
* saved/loaded trees
*/
object TabViewController {
    
    /* List of file names (excluding path and ext) wrapped in Var */
    private val fileNames: Var[List[String]] = Var(Nil)
    val setFileNames: Observer[List[String]] = fileNames.writer
    val concatFileName: Observer[String] = fileNames.updater((names, name) => names :+ name)
    
    /* Index of tab that is currented selected */
    private val selectedTab: Var[Int] = Var(0)
    
    
    /**
    * Gets the scrollable tab element
    */
    def getTabBar: HtmlElement = div()
    
    /**
    * Gets filenames of saved trees
    */
    def getFileNames: Signal[List[String]] = {
        fetchSavedTreeNames().toSignal(Nil)
    }

    /**
      * Returns the title of the selected tab
      */
    def getSelectedTab: Signal[String] = {
        fileNames.signal.combineWithFn(selectedTab.signal)((names, i) => names(i))
    }
    
    /**
      * Sets the shared signal for selected tab to a particular tab title
      *
      * @param tabTitle The title of the tab that has been selected
      */
    def setSelectedTab(tabTitle: String): EventStream[DebugTree] = {
        fileNames.signal
            .map(names => names.indexOf(tabTitle))
            --> selectedTab.writer //FIXME: think this will not fire?

        /* Loads in the respective tree */
        getSelectedTab
            .compose(TabViewController.loadSavedTree(_).toWeakSignal)
            .changes
            .collectSome 
            
        //TODO: check .changes()
    }

    /**
      * Checks if a tab is the currently selected tab
      *
      * @param tabTitle The title of the tab we are checking
      */
    def isSelectedTab(tabTitle: String): Signal[Boolean] = {
        getSelectedTab.map(_ == tabTitle)
    }

    /**
      * Returns true if there are no saved trees
      */
    def noSavedTrees: Signal[Boolean] = {
        getFileNames.signal.map(names => names.isEmpty)
    }

    /**
    * Fetches all tree names saved by the user from the backend
    *
    * @param fileNames List of all trees saved by the user within a session
    */
    def fetchSavedTreeNames(): EventStream[List[String]] = {
        Tauri.invoke(Command.FetchSavedTreeNames, ()).collectRight
    }

    /**
      * Deletes a tab within the tab bar and reloads saved tree names
      *
      * @param treeName The name of the tree belonging to the tab to be deleted
      */
    def deleteTab(name: String): EventStream[List[String]] = {
        Tauri.invoke(Command.DeleteTree, name)
            .collectRight
            .flatMapTo(fetchSavedTreeNames())
    }
    
    /**
    * Saves the the curren tree to the backend as a given name
    *
    * @param treeName User-defined name of tree to be saved
    */
    def saveTree(name: String): EventStream[DebugTree] = {
        Signal.fromValue(name) --> concatFileName //TODO: check fires
        
        Tauri.invoke(Command.SaveTree, name)
            .collectRight
            .flatMapTo(setSelectedTab(name))
    }
    
    /**
    * Loads a saved tree from the backend into the display tree
    *
    * @param treeName User-defined name of the tree to be loaded
    */
    def loadSavedTree(treeName: Signal[String]): EventStream[DebugTree] = {
        treeName.flatMapMerge((name: String) => Tauri.invoke(Command.LoadSavedTree, name)).collectRight
    }

}