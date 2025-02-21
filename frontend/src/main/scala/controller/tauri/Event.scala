package controller.tauri

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree
import model.Deserialize
import model.DillError


sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given Deserialize[DillError, Out] = scala.compiletime.deferred

    type UnlistenFn = () => Unit

    
    /* Listen to backend event using Tauri JS interface */
    private[tauri] def listen(): (EventStream[Either[DillError, Out]], Future[UnlistenFn]) = {
        val (stream, callback) = EventStream.withCallback[String]
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, e => callback(e.payload.toString))
            .toFuture
            .mapTo[() => Unit]

        /* Deserialise event response and map stream to a Tauri.Response stream */
        val responseStream: EventStream[Either[DillError, Out]] = stream
            .map((json: String) => {
                Deserialize[DillError, Out]
                    .read(json, err => DillError(err))
            })

        (responseStream, unlisten)
    }
}


object Event {
    case object TreeReady extends Event("tree-ready") {
        type Out = DebugTree
    }
}


