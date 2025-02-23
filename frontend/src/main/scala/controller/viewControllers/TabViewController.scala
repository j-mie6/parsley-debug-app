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

    /* Set file names */
    def setFileNames: Observer[List[String]] = fileNames.writer
    
    /* Add name to file names */
    def addFileName: Observer[String] = fileNames.updater((names, name) => names :+ name)
    
    /* Fetches all tree names saved by the user from the backend */
    def getFileNames: Signal[List[String]] = {
        Tauri.invoke(Command.FetchSavedTreeNames, ())
            .collectRight
            .toSignal(Nil)
    }


    /* Index of tab that is currented selected */
    private lazy val selectedTab: Var[Int] = Var(0)

    //TODO: see if this can be moved into setSelectedTab()
    def getTab(tabName: String): Signal[Int] = getFileNames.map(_.indexOf(tabName))

    /* Set current selected tab index */
    def setSelectedTab: Observer[Int] = selectedTab.writer
            
    /* Get selected tab name */
    def getSelectedTab: Signal[String] = {
        fileNames.signal
            .withCurrentValueOf(selectedTab.signal)
            .map((names, i) => names(i))
    }

    /* Checks if tab is currently selected */
    def isSelectedTab(tabTitle: String): Signal[Boolean] = getSelectedTab.map(_ == tabTitle)


    /* Returns true if there are no saved trees */
    def noSavedTrees: Signal[Boolean] = getFileNames.signal.map(_.isEmpty)


    /**
      * Deletes a tab within the tab bar and reloads saved tree names
      *
      * @param treeName The name of the tree belonging to the tab to be deleted
      */
    def deleteTab(name: String): EventStream[List[String]] = {
        Tauri.invoke(Command.DeleteTree, name)
            .collectRight
            .sample(getFileNames)
    }
    
    //TODO
    /**
    * Saves the the curren tree to the backend as a given name
    *
    * @param treeName User-defined name of tree to be saved
    */
    def saveTree(name: EventStream[String]): EventStream[DebugTree] = {
        name.flatMapMerge(Tauri.invoke(Command.SaveTree, _))
            .collectRight
            .sample(name.toWeakSignal) //FIXME
            .collectSome
            .compose(loadSavedTree(_))
    }

    /**
    * Loads a saved tree from the backend into the display tree
    *
    * @param treeName User-defined name of the tree to be loaded
    */
    def loadSavedTree(treeName: EventStream[String]): EventStream[DebugTree] = {
        treeName.flatMapMerge((name: String) => Tauri.invoke(Command.LoadSavedTree, name)).collectRight
    }

}