package controller.tauri

import upickle.default as up
import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}
import org.scalablytyped.runtime.StringDictionary
import com.raquo.laminar.api.L.*
import model.DebugTree
import model.DebugNode


sealed trait Command(val name: String) {
    //TODO: make arguments Command path dependent
    // type Args 

    //TODO: handle errors - i.e., what to do when up.Read fails
    type Response
    given up.Reader[Response] = scala.compiletime.deferred 


    /** 
     * Invoke specified backend Tauri command.
     *
     * @param cmd Name of Tauri command to invoke.
     * @return EventStream holding the result of the command call
     */
    protected[tauri] def invoke(): EventStream[this.Response] = this.invoke(Map())


    /**
     * Invoke specified backend Tauri command with arguments.
     *
     * @param cmd Name of Tauri command to invoke
     * @param args Map of argument name to argument value. Argument names should be CamelCase.
     * @return EventStream holding the result of the command call
     */
    protected[tauri] def invoke(args: Map[String, Any]): EventStream[this.Response] = 
        EventStream.fromJsPromise(
            tauriInvoke[String](this.name, StringDictionary(args.toSeq*)),
            emitOnce = true
        ).map((json: String) => up.read[this.Response](json))

}


object Command {
    /* Fetch commands */
    case object FetchDebugTree extends Command("fetch_debug_tree"):
        type Response = DebugTree
    
    case object FetchNodeChildren extends Command("fetch_node_children"):
        type Response = List[DebugNode]


    /* Save commands */
    case object SaveTree extends Command("save_tree"):
        type Response = Unit

    case object FetchSavedTreeNames extends Command("fetch_saved_tree_names"):
        type Response = List[String]

    case object LoadSavedTree extends Command("load_saved_tree"): 
        type Response = DebugTree
}




