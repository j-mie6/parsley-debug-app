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
    type Response
    given up.Reader[Response] = scala.compiletime.deferred 
    

    /* Helper function for no-arg invoke */
    protected[tauri] def invoke(): EventStream[Try[this.Response]] = this.invoke(Map())

    /* Invoke backend command using Tauri JS interface */
    protected[tauri] def invoke(args: Map[String, Any]): EventStream[Try[this.Response]] = {
        /* Invoke command with arguments passed as JS string dictionary */
        val invoke: js.Promise[String] = tauriInvoke[String](this.name, StringDictionary(args.toSeq*));
        
        /* Start EventStream from promise with value parsed to Response type */
        EventStream.fromJsPromise(invoke, emitOnce = true)
            .map((json: String) => Try(up.read[this.Response](json)))
    }

}


object Command {

    /* Fetch commands */

    case object FetchDebugTree extends Command("fetch_debug_tree"):
        type Response = DebugTree
    
    case object FetchNodeChildren extends Command("fetch_node_children"):
        type Response = List[DebugNode]


    /* Save commands */

    case object SaveTree extends Command("save_tree"):
        type Response = None.type

    case object FetchSavedTreeNames extends Command("fetch_saved_tree_names"):
        type Response = List[String]

    case object LoadSavedTree extends Command("load_saved_tree"): 
        type Response = DebugTree
}




