package controller.tauri

import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree
import model.json.{Reader, JsonError}
import controller.tauri.Event.UnlistenFn
import model.errors.MalformedJSON
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
        // val responseStream: EventStream[Either[DillException, Out]] = stream
        //     .map(response => (Try(up.read[Out](response))) match {
        //         case Success(output) => Right(output)
        //         case Failure(_) if this.isUnit && response == null => Right(().asInstanceOf[Out]) 
        //         case Failure(err) => Left(ErrorController.mapException(err))
        //     })  
            
        val responseStream: EventStream[Either[DillException, Out]] = stream
            .map((json: String) => Reader[Out].read(json)
                .swap
                .map(_ => ErrorController.mapException(new Exception(json))) //TODO: map exception properly in Json.scala
                .swap
            )

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

}


