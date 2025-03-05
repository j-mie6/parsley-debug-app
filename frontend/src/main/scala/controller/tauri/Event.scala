package controller.tauri

import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.{DebugTree, CodeFileInformation}
import model.json.Reader
import controller.tauri.Event.UnlistenFn
import model.errors.DillException
import controller.errors.ErrorController


/**
 * Trait defining event functionality, such as the output type, listen/unlistening
 * function and how to read the JSON received.
 * 
 * @param name Name of the event
 */
sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given Reader[Out] = scala.compiletime.deferred

    /* Determines whether we should ignore empty JSON errors */
    val isUnit: Boolean = false
    
    /* Listen to backend event using Tauri JS interface */
    private[tauri] def listen(): (EventStream[Either[DillException, Out]], Future[UnlistenFn]) = {
        val (stream, callback) = EventStream.withCallback[String]
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, e => callback(e.payload.toString))
            .toFuture
            .mapTo[() => Unit]

        /* Deserialise event response and map stream to a Tauri.Response stream */
        val responseStream: EventStream[Either[DillException, Out]] = stream
            .recoverToEither
            .mapLeft(ErrorController.mapException)
            .map(_.flatMap((json: String) => Reader[Out].read(json)))

        (responseStream, unlisten)
    }
}

/**
  * Companion object of Event, which defines the different possible events
  */
object Event {
    type UnlistenFn = () => Unit

    case object TreeReady extends Event("tree-ready") {
        type Out = DebugTree
    }

    case object NewTree extends Event("new-tree") {
        type Out = Unit
        override val isUnit = true
    }

    case object UploadCodeFile extends Event("upload-code-file") {
        type Out = String
    }

    case object UploadFiles extends Event("upload-code-files") {
        type Out = CodeFileInformation
    }
}


