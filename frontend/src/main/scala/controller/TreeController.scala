package controller

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import upickle.default as up

import controller.tauri.{Tauri, Command}
import model.DebugTree
import view.DebugTreeDisplay


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeController {
    /* Deserialise the JSON string into a DebugTree */
    private def deserialise(json: String): Option[DebugTree] = DebugTreeHandler.decodeDebugTree(json).toOption
    
    /* Convert the DebugTree into a HtmlElement */
    private def display(tree: Option[DebugTree]): Option[HtmlElement] = tree.map(DebugTreeDisplay(_))

    /**
      * Fetch the debug tree from the tauri backend
      *
      * @return Stream containing an optional HTML Element DebugTreeDisplay to be rendered.
      */
    def load: EventStream[Option[HtmlElement]] = Tauri.invoke[String](Command.FetchDebugTree)
        .map(deserialise)
        .map(display)


    /** 
     * Fetch saved trees
     * 
     * @return Stream containing saved tree names
     */
    def fetchSavedTreeNames(): EventStream[List[String]] = Tauri.invoke[String](Command.FetchSavedTreeNames)
        .map(up.read[List[String]](_))
        
    /**
      * Save tree into JSON file
      *
      * @param treeName Name to save the tree with 
      */
    def saveTree(treeName: String): Unit = Tauri.invoke[Unit](Command.SaveTree, Map("name" -> treeName))

    /**
      * Load saved tree into view
      *
      * @param treeName Name of tree to load
      */
    def loadSavedTree(treeName: String): EventStream[Option[HtmlElement]] = 
        Tauri.invoke[String](Command.LoadSavedTree, Map("name" -> treeName))
            .map(deserialise)
            .map(display)

}
