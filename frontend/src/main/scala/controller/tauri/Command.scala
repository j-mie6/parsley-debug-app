package controller.tauri

import scala.util.{Try, Success, Failure}
import scala.scalajs.js

import com.raquo.laminar.api.L.*
import org.scalablytyped.runtime.StringDictionary
import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}

import model.{DebugNode, DebugTree}
import controller.tauri.Args
import model.json.Reader
import model.json.JsonError
import model.errors.DillException
import controller.errors.ErrorController


/* Argument trait implemented for types passed to Command invoke call */ 
private [tauri] sealed trait Args[A] {
    extension (a: A) 
        def namedArgs: Map[String, Any]
}


private [tauri] object Args {
    /* Default conversion from Unit to empty Arg Map */
    given noArgs: Args[Unit] {
        extension (unit: Unit) 
            def namedArgs: Map[String, Any] = Map()
    }
}


/* Command trait implemented for each invokable Tauri command */ 
sealed trait Command(private val name: String) {

    /* Argument type associated with Command */
    type In
    given args: Args[In]

    /* Response type associated with Command */
    type Out
    given Reader[Out] = scala.compiletime.deferred 

    
    /* Invoke backend command using Tauri JS interface */
    private [tauri] def invoke(args: In): EventStream[Either[DillException, Out]] = {
        val strArgs: StringDictionary[Any] = StringDictionary(args.namedArgs.toSeq*)

        /* Invoke command with arguments passed as JS string dictionary */
        val invoke: js.Promise[String] = tauriInvoke[String](name, strArgs);
        
        /* Start EventStream from invoke and deserialise invoke response */
        EventStream.fromJsPromise(invoke, emitOnce = true)
            .map((json: String) => Reader[Out].read(json)
                .swap
                .map(_ => ErrorController.mapException(JsonError("")))
                .swap
            )
    }

}


object Command {

    /* Fetch commands */
    case object FetchDebugTree extends Command("fetch_debug_tree") {
        type In = Unit
        given args: Args[In] = Args.noArgs

        type Out = DebugTree
    } 

    case object FetchNodeChildren extends Command("fetch_node_children") {
        type In = Int
        given args: Args[Int] {
            extension (id: Int) 
                def namedArgs: Map[String, Any] = Map("nodeId" -> id)
        }

        type Out = List[DebugNode]
    }


    /* Save commands */
    case object SaveTree extends Command("save_tree") {
        type In = String
        given args: Args[String] {
            extension (treeName: String)
                def namedArgs: Map[String, Any] = Map("treeName" -> treeName)
        }

        type Out = Unit
        // override val isUnit: Boolean = true TODO: remove
    }

    case object FetchSavedTreeNames extends Command("fetch_saved_tree_names") {
        type In = Unit
        given args: Args[In] = Args.noArgs 

        type Out = List[String]
    }

    case object LoadSavedTree extends Command("load_saved_tree") {
        type In = String
        given args: Args[String] {
            extension (treeName: String)
                def namedArgs: Map[String, Any] = Map("treeName" -> treeName)
        }

        type Out = Unit
        // override val isUnit: Boolean = true TODO: remove
    }

    case object DeleteTree extends Command("delete_tree") {
        type In = String
        given args: Args[String] {
            extension (treeName: String)
                def namedArgs: Map[String, Any] = Map("treeName" -> treeName)
        }

        type Out = Unit
        // override val isUnit: Boolean = true TODO: remove
    }

}
