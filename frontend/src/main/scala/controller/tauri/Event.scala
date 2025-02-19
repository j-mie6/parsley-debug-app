package controller.tauri

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import upickle.default as up
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree
import Tauri.Response


sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given up.Reader[Out] = scala.compiletime.deferred

    type UnlistenFn = () => Unit

    
    /* Listen to backend event using Tauri JS interface */
    protected[tauri] def listen(): (EventStream[Response[Out]], Future[UnlistenFn]) = {
        val (stream, callback) = EventStream.withCallback[String]
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, e => callback(e.payload.toString()))
            .toFuture
            .mapTo[() => Unit]

        /* Deserialise event response and map stream to a Tauri.Response stream */
        val responseStream: EventStream[Response[Out]] = stream
            .map(up.read[Out](_).nn)
            .recoverToEither
            .mapLeft(error => new Tauri.Error("Parsing event payload failed! " + error.toString()))

        (responseStream, unlisten)
    }
}


object Event {
    case object TreeReady extends Event("tree-ready"):
        type Out = DebugTree
}


