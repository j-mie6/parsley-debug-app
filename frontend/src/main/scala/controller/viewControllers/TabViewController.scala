package controller.viewControllers

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import controller.tauri.{Tauri, Command}
import controller.viewControllers.TreeViewController
import model.DebugTree
import model.errors.DillException


/**
* TabViewController object contains functions that modify the state of 
* saved/loaded trees
*/
object TabViewController {
    
    /* List of file names (excluding path and ext) wrapped in Var */
    private val fileNames: Var[List[String]] = Var(Nil)

    /** Get file name associated with tab at index */
    def getFileName(index: Int): Signal[String] = {
        println(s"Inside getFileName for index: $index")
        fileNames.signal.map(_(index))
    }

    /** Get list of file names */
    val getFileNames: Signal[List[String]] = {
        fileNames.signal
    }

    /** Set file names */
    val setFileNames: Observer[List[String]] = fileNames.writer
    
    /** Add name to file names */
    val addFileName: Observer[String] = fileNames.updater((names, name) => names :+ name)

    /** Remove name from file names */
    val removeFileName: Observer[String] = fileNames.updater((names, name) => names.filter(_ != name))
    
    /** Fetches all tree names saved by the user from the backend */
    def loadFileNames: EventStream[Either[DillException, List[String]]] = Tauri.invoke(Command.FetchSavedTreeNames, ())

    /** Returns true if there are no saved trees */
    def noSavedTrees: Signal[Boolean] = getFileNames.signal.map(_.isEmpty)

    
    /* Index of tab that is currented selected */
    private lazy val selectedTab: Var[Int] = Var(0)

    /** Set current selected tab index */
    val setSelectedTab: Observer[Int] = {
        println("In set selected tab")
        selectedTab.writer
    }

    /** Get index of tab with associated name */
    def getFileNameIndex(name: String): Signal[Int] = getFileNames.map(_.indexOf(name))
            
    /** Get selected tab index */
    val getSelectedTab: Signal[Int] = {
        println(s"Inside getSelectedTab for tab # ${selectedTab.now()}")
        selectedTab.signal
    }
    
    /** Get name of tree associated with selected tab */
    def getSelectedFileName: Signal[String] = {
        println("Inside getSelectedFileName")
        getSelectedTab.flatMapSwitch(getFileName)
    }

    /** Checks if tab is currently selected */
    def tabSelected(index: Int): Signal[Boolean] = getSelectedTab.map(_ == index)


    /** Saves current tree to the backend with given name, returning assigned tab index  */
    def saveTree(name: String): EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.SaveTree, name)

    /** Loads a saved tree from the backend as DebugTree */
    def loadSavedTree(name: String): EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.LoadSavedTree, name)

    /** Delete tree loaded within tab, returning updated list of names */
    def deleteSavedTree(name: String): EventStream[Either[DillException, Unit]] = {
        Tauri.invoke(Command.DeleteTree, name)
        // loadFileNames --> setFileNames
    }

}