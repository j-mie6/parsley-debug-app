package controller.tauri

import scala.util.{Try, Success, Failure}
import scala.scalajs.js

import com.raquo.laminar.api.L.*
import org.scalablytyped.runtime.StringDictionary
import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}

import model.{DebugNode, DebugTree}
import model.errors.DillException
import model.json.Reader
import controller.errors.ErrorController
import controller.tauri.Args


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
        val stringDict: StringDictionary[Any] = StringDictionary(args.namedArgs.toSeq*)

        /* Start EventStream from JS Tauri invoke with argument passed as a string dictionary */
        EventStream.fromJsPromise(tauriInvoke[String](name, stringDict), emitOnce = true)
            .recoverToEither                                            /* Catch backend exceptions */
            .mapLeft(ErrorController.mapException)                      /* Map exception to a DillException */
            .map(_.flatMap((json: String) => Reader[Out].read(json)))   /* Deserialise response using deferred Reader */
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
        given args: Args[In] {
            extension (id: Int) 
                def namedArgs: Map[String, Any] = Map("nodeId" -> id)
        }

        type Out = List[DebugNode]
    }


    /* Save commands */
    case object SaveTree extends Command("save_tree") {
        type In = String
        given args: Args[In] {
            extension (treeName: String)
                def namedArgs: Map[String, Any] = Map("treeName" -> treeName)
        }

        type Out = List[String]
    }

    case object LoadSavedTree extends Command("load_saved_tree") {
        type In = Int
        given args: Args[In] {
            extension (index: Int)
                def namedArgs: Map[String, Any] = Map("index" -> index)
        }

        type Out = Unit
    }

    case object DeleteTree extends Command("delete_tree") {
        type In = Int
        given args: Args[In] {
            extension (index: Int)
                def namedArgs: Map[String, Any] = Map("index" -> index)
        }

        type Out = List[String]
    }

    case object DownloadTree extends Command("download_tree") {
        type In = String
        given args: Args[String] {
            extension (treeName: String)
                def namedArgs: Map[String, Any] = Map("treeName" -> treeName)
        }

        type Out = Unit
    }

    case object ImportTree extends Command("import_tree") {
        type In = (String, String)
        given args: Args[In] {
            extension (fileInfo: (String, String))
                def namedArgs: Map[String, Any] = Map("treeName" -> fileInfo._1, "contents" -> fileInfo._2)
        }

        type Out = Unit
    }

    case object SkipBreakpoints extends Command("skip_breakpoints") {
        type In = (Int, Int, Seq[(Int, String)])
        given args: Args[In] {
            extension (args: (Int, Int, Seq[(Int, String)]))
                def namedArgs: Map[String, Any] = Map("sessionId" -> args._1, "skips" -> args._2, "newRefs" -> js.Array(args._3.map(js.Tuple2(_, _))*))
        }
        type Out = Unit
    }

    case object RequestSourceFile extends Command("request_source_file") {
        type In = String
        given args: Args[In] {
            extension (filePath: String)
                def namedArgs: Map[String, Any] = Map("filePath" -> filePath)
        }
        type Out = Unit
    }
}
