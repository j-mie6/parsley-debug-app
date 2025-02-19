package controller.tauri

import scala.util.Try
import scala.scalajs.js

import com.raquo.laminar.api.L.*
import upickle.default as up
import org.scalablytyped.runtime.StringDictionary
import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}

import model.DebugTree
import model.DebugNode


sealed trait Command(private val name: String) {

    /* Argument type associated with Command */
    type In
    given Conversion[In, Map[String, Any]] = scala.compiletime.deferred

    /* Response type associated with Command */
    type Out
    given up.Reader[Out] = scala.compiletime.deferred 

    
    /* Invoke backend command using Tauri JS interface */
    protected[tauri] def invoke(args: In): EventStream[Tauri.Response[Out]] = {
        val strArgs: StringDictionary[Any] = StringDictionary(args.toMap.toSeq*)

        /* Invoke command with arguments passed as JS string dictionary */
        val invoke: js.Promise[String] = tauriInvoke[String](name, strArgs);
        
        /* Start EventStream from invoke and deserialise invoke response */
        EventStream.fromJsPromise(invoke, emitOnce = true)
            .map(up.read[Out](_).nn)
            .recoverToEither
            .mapLeft(error => new Tauri.Error("Parsing command response failed! " + error.toString()))
    }

}


object Command {

    /* Default conversion from Unit to empty Arg Map */
    given Conversion[Unit, Map[String, Any]] = { _ => Map() }


    /* Fetch commands */

    case object FetchDebugTree extends Command("fetch_debug_tree") {
        type In = Unit
        type Out = DebugTree
    }

    case object FetchNodeChildren extends Command("fetch_node_children") {
        type In = Unit
        type Out = List[DebugNode]
    }


    /* Save commands */

    case object SaveTree extends Command("save_tree") {
        type In = String
        override given Conversion[In, Map[String, Any]] = {
            (name: String) => Map("treeName" -> name)
        }

        type Out = Unit
    }

    case object FetchSavedTreeNames extends Command("fetch_saved_tree_names") {
        type In = Unit
        type Out = List[String]
    }

    case object LoadSavedTree extends Command("load_saved_tree") {
        type In = String
        override given Conversion[In, Map[String, Any]] = { 
            (name: String) => Map("treeName" -> name)
        }

        type Out = DebugTree
    }

}
