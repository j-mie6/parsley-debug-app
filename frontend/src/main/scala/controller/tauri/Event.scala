package controller.tauri

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree
import model.Deserialize
import model.JsonError


sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given Deserialize[Out] = scala.compiletime.deferred

    type UnlistenFn = () => Unit

    
    /* Listen to backend event using Tauri JS interface */
    private[tauri] def listen(): (EventStream[Either[JsonError, Out]], Future[UnlistenFn]) = {
        val (stream, callback) = EventStream.withCallback[String]
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, e => callback(e.payload.toString))
            .toFuture
            .mapTo[() => Unit]

        /* Deserialise event response and map stream to a Tauri.Response stream */
        val responseStream: EventStream[Either[JsonError, Out]] = stream
            .map((json: String) => Deserialize[Out].read(json))

        (responseStream, unlisten)
    }
}


object Event {
    case object TreeReady extends Event("tree-ready") {
        type Out = DebugTree
    }
}


