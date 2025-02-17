package controller

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import upickle.default as up

import controller.tauri.{Tauri, Command}

import view.DebugTreeDisplay


/**
  * Object containing methods for manipulating the DebugTree.
  */
object TreeController {
    
    /**
      * Fetch the debug tree root from the tauri backend.
      *
      * @param displayTree The var that the display tree HTML element will be written into.
      */
    def reloadTree(displayTree: Var[HtmlElement]): Unit = 
        for {
            treeString <- Tauri.invoke[String](Command.FetchDebugTree.name)
        } do {
            displayTree.set(
                if treeString.isEmpty then div("No tree found") else 
                    DebugTreeHandler.decodeDebugTree(treeString) match {
                        case Failure(exception) => println(s"Error in decoding debug tree : ${exception.getMessage()}"); div()
                        case Success(debugTree) => DebugTreeDisplay(debugTree)
                    }
            )
        }

    def saveTree(treeName: String): Unit = Tauri.invoke[String](Command.SaveTree.name, Map("name" -> treeName))

    def fetchSavedTreeNames(fileNames: Var[List[String]]): Unit = {
        Tauri.invoke[String](Command.FetchSavedTreeNames.name).foreach { serializedNames =>
            // Update fileNames with parsed names
            fileNames.update(_ => up.read[List[String]](serializedNames))
        }
    }

    def loadSavedTree(treeName: String, displayTree: Var[HtmlElement]): Unit = {
        Tauri.invoke[String](Command.LoadSavedTree.name, Map("name" -> treeName)).foreach { _ =>
            TreeController.reloadTree(displayTree)
        }
    }
        
}