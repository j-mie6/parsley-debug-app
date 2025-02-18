package controller.tauri

import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import upickle.default as up
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree


sealed trait Event(private val name: String) {

    /* Response type associated with Event */
    type Response
    given up.Reader[Response] = scala.compiletime.deferred

    
    /* Listen to backend event using Tauri JS interface */
    protected[tauri] def listen(): (EventStream[Try[this.Response]], Future[() => Unit]) = {
        val (stream, callback) = EventStream.withCallback[Try[this.Response]]
        
        /* Send deserialised event to the EventStream when Tauri notified */
        val fireEvent = (e: TauriEvent[?]) => callback(Try(up.read[this.Response](e.payload.toString)));
        
        /* Call Tauri function and get future of unlisten function */
        val unlisten = tauriListen(this.name, fireEvent)
            .toFuture
            .mapTo[() => Unit]
            
        (stream, unlisten)
    }
}


object Event {
    case object TreeReady extends Event("tree-ready"):
        type Response = DebugTree
}


