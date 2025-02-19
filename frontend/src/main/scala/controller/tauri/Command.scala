package controller.tauri

import scala.util.Try
import scala.scalajs.js

import com.raquo.laminar.api.L.*
import upickle.default as up
import org.scalablytyped.runtime.StringDictionary
import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}

import model.DebugTree
import model.DebugNode


sealed trait Command(val name: String) {

    /* Response type associated with Command */
    type Out
    given up.Reader[Out] = scala.compiletime.deferred 
    

    /* Helper function for no-arg invoke */
    protected[tauri] def invoke(): EventStream[Tauri.Response[Out]] = this.invoke(Map())

    /* Invoke backend command using Tauri JS interface */
    protected[tauri] def invoke(args: Map[String, Any]): EventStream[Tauri.Response[Out]] = {
        
        /* Invoke command with arguments passed as JS string dictionary */
        val invoke: js.Promise[String] = tauriInvoke[String](name, StringDictionary(args.toSeq*));
        
        /* Start EventStream from promise with value parsed to Output type */
        EventStream.fromJsPromise(invoke, emitOnce = true)
            .map(up.read[Out](_).nn)
            .recoverToEither
            .mapLeft(error => new Tauri.Error("Parsing command response failed! " + error.toString()))
    }

}


object Command {

    /* Fetch commands */

    case object FetchDebugTree extends Command("fetch_debug_tree"):
        type Out = DebugTree
    
    case object FetchNodeChildren extends Command("fetch_node_children"):
        type Out = List[DebugNode]


    /* Save commands */

    case object SaveTree extends Command("save_tree"):
        type Out = Unit

    case object FetchSavedTreeNames extends Command("fetch_saved_tree_names"):
        type Out = List[String]

    case object LoadSavedTree extends Command("load_saved_tree"): 
        type Out = DebugTree
}




