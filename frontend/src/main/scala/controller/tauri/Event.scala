package controller.tauri

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import upickle.default as up
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree


sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given up.Reader[Out] = scala.compiletime.deferred

    type UnlistenFn = () => Unit

    
    /* Listen to backend event using Tauri JS interface */
    private[tauri] def listen(): (EventStream[Either[Tauri.Error, Out]], Future[UnlistenFn]) = {
        val (stream, callback) = EventStream.withCallback[String]
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, e => callback(e.payload.toString))
            .toFuture
            .mapTo[() => Unit]

        /* Deserialise event response and map stream to a Tauri.Response stream */
        val responseStream: EventStream[Either[Tauri.Error, Out]] = stream
            .map(up.read[Out](_).nn)
            .recoverToEither
            .mapLeft(error => "Parsing event payload failed! " + error.toString)

        (responseStream, unlisten)
    }
}


object Event {
    case object TreeReady extends Event("tree-ready") {
        type Out = DebugTree
    }

    case object NewTree extends Event("new-tree") {
        type Out = Unit
    }

}


