package controller

import com.raquo.laminar.api.L.*

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import upickle.default as up

import controller.TreeController


/**
* TabController object contains functions that modify the state of 
* saved/loaded trees
*/

object TabController {
    /* Scrollable tab that holds all saved trees */
    private val scrollableTab: Var[HtmlElement] = Var(div())
    
    /* List of file names (excluding path and ext) wrapped in Var */
    private var fileNames: Var[List[String]] = Var(Nil)

    /* The tab that is currented selected */
    private val selectedTab: Var[String] = Var("")

    
    /**
    * Gets the scrollable tab element
    */
    def getTabBar: Var[HtmlElement] = scrollableTab
    
    /**
      * Sets tab bar element
      *
      * @param tab An individual tab comprising of title and delete button
      */
    def setTabBar(tab: HtmlElement): Unit = {
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
      * Returns the title of the selected tab
      */
    def getSelectedTab: Var[String] = selectedTab

    /**
      * Sets the shared signal for selected tab to a particular tab title
      *
      * @param tabTitle The title of the tab that has been selected
      */
    def setSelectedTab(tabTitle: String): Unit = {
        selectedTab.set(tabTitle)
        TreeController.reloadTree()
    } 

    /**
      * Checks if a tab is the currently selected tab
      *
      * @param tabTitle The title of the tab we are checking
      */
    def isSelectedTab(tabTitle: String): Signal[Boolean] = {
        selectedTab.signal.map(selected => selected == tabTitle)
    }

    /**
      * Returns true if there are no saved trees
      */
    def hasNoTabs: Signal[Boolean] = 
        getFileNames.signal.map(names => names.isEmpty)
    
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
      * Deletes a tab within the tab bar and reloads saved tree names
      *
      * @param tabTitle
      */
    def deleteTab(tabTitle: String): Unit = {
        Tauri.invoke("delete_tree", Map("tree_name" -> tabTitle))
        fetchSavedTreeNames()
    }
    
    /**
    * 
    *
    * @param treeName User-defined name of tree to be saved
    */
    def saveTree(treeName: String): Unit = {
        Tauri.invoke[String]("save_tree", Map("name" -> treeName))
        fetchSavedTreeNames()
        setSelectedTab(treeName)
    }
    
    /**
    * 
    *
    * @param treeName User-defined name of the tree to be loaded
    * @param displayTree Tree element to load and display in a given tree
    */
    def loadSavedTree(treeName: String): Unit = {
        Tauri.invoke[String]("load_saved_tree", Map("tree_name" -> treeName)).foreach { _ =>
            TreeController.reloadTree()
        }
    }
}