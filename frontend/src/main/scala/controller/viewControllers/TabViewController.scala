package controller.viewControllers

import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*

import upickle.default as up

import controller.tauri.{Tauri, Command}
import controller.viewControllers.TreeViewController



/**
* TabViewController object contains functions that modify the state of 
* saved/loaded trees
*/

object TabViewController {
    /* Scrollable tab that holds all saved trees */
    private val scrollableTab: Var[HtmlElement] = Var(div())
    
    /* List of file names (excluding path and ext) wrapped in Var */
    private val fileNames: Var[List[String]] = Var(Nil)

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
    def setTabBar(tab: HtmlElement): Unit = scrollableTab.set(tab)
    
    
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

        /* Loads in the respective tree */
        TabViewController.loadSavedTree(tabTitle) 

        /* Reload the tree */
        TreeViewController.reloadTree()
    } 

    /**
      * Checks if a tab is the currently selected tab
      *
      * @param tabTitle The title of the tab we are checking
      */
    def isSelectedTab(tabTitle: String): Signal[Boolean] =
        selectedTab.signal.map(selected => selected == tabTitle)

    /**
      * Returns true if there are no saved trees
      */
    def noSavedTrees: Signal[Boolean] = 
        getFileNames.signal.map(names => names.isEmpty)
    
    /**
    * Fetches all tree names saved by the user from the backend
    *
    * @param fileNames List of all trees saved by the user within a session
    */
    def fetchSavedTreeNames(): Unit = {
        Tauri.invoke[String](Command.FetchSavedTreeNames).foreach { serializedNames =>
            /* Update fileNames with parsed names */
            fileNames.update(_ => up.read[List[String]](serializedNames))
        }
    }
    
    /**
      * Deletes a tab within the tab bar and reloads saved tree names
      *
      * @param treeName The name of the tree belonging to the tab to be deleted
      */
    def deleteTab(name: String): Unit = {
        Tauri.invoke[Unit](Command.DeleteTree, Map("treeName" -> name))
        fetchSavedTreeNames()
    }
    
    /**
    * Saves the the curren tree to the backend as a given name
    *
    * @param treeName User-defined name of tree to be saved
    */
    def saveTree(name: String): Unit = {
        Tauri.invoke[String](Command.SaveTree, Map("treeName" -> name))
        fetchSavedTreeNames()
        setSelectedTab(name)
    }
    
    /**
    * Loads a saved tree from the backend into the display tree
    *
    * @param treeName User-defined name of the tree to be loaded
    * @param displayTree Tree element to load and display in a given tree
    */
    def loadSavedTree(treeName: String): Unit = 
        Tauri.invoke[String](Command.LoadSavedTree, Map("treeName" -> treeName))
          .foreach { _ =>
            TreeViewController.reloadTree()
        }
}