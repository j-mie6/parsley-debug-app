package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import upickle.default as up


/**
* TabController object contains functions that modify the state of 
* saved/loaded trees
*/

object TabController {
    /* Scrollable tab that holds all saved trees */
    private val scrollableTab: Var[HtmlElement] = Var(div())
    
    /* List of file names (excluding path and ext) wrapped in Var */
    private var fileNames: Var[List[String]] = Var(Nil)
    
    /**
    * Gets the scrollable tab element
    */
    def getTab = scrollableTab
    
    /**
      * 
      *
      * @param tab An individual tab comprising of title and delete button
      */
    def setTab(tab: HtmlElement) = {
        scrollableTab.set(tab)
    }
    
    /**
    * Gets filenames of saved trees
    */
    def getFileNames: Var[List[String]] = {
        fetchSavedTreeNames()
        fileNames
    }
    
    
    /**
    * 
    *
    * @param fileNames List of all trees saved by the user within a session
    */
    def fetchSavedTreeNames(): Unit = {
        Tauri.invoke[String]("fetch_saved_tree_names").foreach { serializedNames =>
            /* Update fileNames with parsed names */
            fileNames.update(_ => up.read[List[String]](serializedNames))
        }
    }
    
    /**
      * 
      *
      * @param tabTitle
      */
    def deleteTab(tabTitle: String): Unit = {
        Tauri.invoke("delete_tree", Map("name" -> tabTitle))
        fetchSavedTreeNames()
        TreeController.reloadTree()
    }
    
    /**
    * 
    *
    * @param treeName User-defined name of tree to be saved
    */
    def saveTree(treeName: String): Unit = {
        Tauri.invoke[String]("save_tree", Map("name" -> treeName))
        fetchSavedTreeNames()
    }
    
    /**
    * 
    *
    * @param treeName User-defined name of the tree to be loaded
    * @param displayTree Tree element to load and display in a given tree
    */
    def loadSavedTree(treeName: String): Unit = {
        Tauri.invoke[String]("load_saved_tree", Map("name" -> treeName)).foreach { _ =>
            TreeController.reloadTree()
        }
    }
}