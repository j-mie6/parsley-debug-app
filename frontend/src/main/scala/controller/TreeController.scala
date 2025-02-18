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
    /* Reactive DebugTree */
    private val treeVar: Var[Option[DebugTree]] = Var(None)
    val tree: Observer[Option[DebugTree]] = treeVar.writer

    /* Convert the DebugTree into a HtmlElement */
    val treeElem: Signal[Option[HtmlElement]] = treeVar.signal.map(_.map(DebugTreeDisplay(_)))

    /* Deserialise the JSON string into a DebugTree */
    private def deserialise(json: String): Option[DebugTree] = DebugTreeHandler.decodeDebugTree(json).toOption

    /**
      * Fetch the debug tree from the tauri backend
      *
      * @return Stream containing an optional HTML Element DebugTreeDisplay to be rendered.
      */
    def load: EventStream[Option[DebugTree]] = Tauri.invoke[String](Command.FetchDebugTree)
        .map(deserialise)


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
    def loadSavedTree(treeName: String): EventStream[Option[DebugTree]] = 
        Tauri.invoke[String](Command.LoadSavedTree, Map("name" -> treeName))
            .map(deserialise)

}
