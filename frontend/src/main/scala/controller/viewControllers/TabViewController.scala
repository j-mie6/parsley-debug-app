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
    def getFileName(index: Int): Signal[String] = fileNames.signal.map(_(index))

    /** Get list of file names */
    val getFileNames: Signal[List[String]] = fileNames.signal

    /** Set file names */
    val setFileNames: Observer[List[String]] = fileNames.writer

    /** Returns true if there are no saved trees */
    def noSavedTrees: Signal[Boolean] = getFileNames.signal.map(_.isEmpty)


    /* Index of tab that is currented selected */
    private lazy val selectedTab: Var[Int] = Var(0)

    /** Set current selected tab index */
    val setSelectedTab: Observer[Int] = selectedTab.writer

    /** Get selected tab index */
    val getSelectedTab: Signal[Int] = {
        println(s"Inside getSelectedTab for tab # ${selectedTab.now()}")
        selectedTab.signal
    }
    
    /** Checks if tab is currently selected */
    def tabSelected(index: Int): Signal[Boolean] = getSelectedTab.map(_ == index)


    /** Saves current tree to the backend with given name, returning assigned tab index  */
    def saveTree(name: String): EventStream[Either[DillException, List[String]]] = Tauri.invoke(Command.SaveTree, name)

    /** Loads a saved tree from the backend as DebugTree */
    def loadSavedTree(index: Int): EventStream[Either[DillException, Unit]] = Tauri.invoke(Command.LoadSavedTree, index)

    /** Delete tree loaded within tab, returning updated list of names */
    def deleteSavedTree(index: Int): EventStream[Either[DillException, List[String]]] = Tauri.invoke(Command.DeleteTree, index)
}