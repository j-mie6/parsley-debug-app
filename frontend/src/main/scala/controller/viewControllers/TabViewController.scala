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

    /* Get file name associated with tab at index */
    def getFileName(index: Int): Signal[String] = fileNames.signal.map(_(index))

    /* Get list of file names */
    def getFileNames: Signal[List[String]] = fileNames.signal

    /* Set file names */
    def setFileNames: Observer[List[String]] = fileNames.writer
    
    /* Add name to file names */
    def addFileName: Observer[String] = fileNames.updater((names, name) => names :+ name)
    
    /* Fetches all tree names saved by the user from the backend */
    def loadFileNames: Signal[List[String]] = {
        Tauri.invoke(Command.FetchSavedTreeNames, ())
            .collectRight
            .toSignal(Nil)
    }

    /* Returns true if there are no saved trees */
    def noSavedTrees: Signal[Boolean] = getFileNames.signal.map(_.isEmpty)

    
    /* Index of tab that is currented selected */
    private lazy val selectedTab: Var[Int] = Var(0)

    /* Set current selected tab index */
    def setSelectedTab: Observer[Int] = selectedTab.writer

    /* Get index of tab with associated name */
    def getFileNameIndex(name: String): Signal[Int] = getFileNames.map(_.indexOf(name))
            
    /* Get selected tab index */
    def getSelectedTab: Signal[Int] = selectedTab.signal

    /* Checks if tab is currently selected */
    def tabSelected(index: Int): Signal[Boolean] = getSelectedTab.map(_ == index)


    /* Saves current tree to the backend with given name, returning assigned tab index  */
    def saveTree(name: String): EventStream[Unit] = Tauri.invoke(Command.SaveTree, name).collectRight

    /* Loads a saved tree from the backend as DebugTree */
    def loadSavedTree(name: EventStream[String]): EventStream[DebugTree] = {
        name.flatMapMerge(Tauri.invoke(Command.LoadSavedTree, _))
            .collectRight
    }

    /* Delete tree loaded within tab, returning updated list of names */
    def deleteTab(name: EventStream[String]): EventStream[List[String]] = {
        name.flatMapMerge(Tauri.invoke(Command.DeleteTree, _))
            .collectRight
            .sample(loadFileNames)
    }

}