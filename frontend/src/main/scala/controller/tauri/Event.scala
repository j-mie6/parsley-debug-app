package controller.tauri

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import upickle.default as up
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree


sealed trait Event(private val name: String) {
    type Response

    //TODO: handle errors - i.e., what to do when up.Read fails
    given up.Reader[Response] = scala.compiletime.deferred

    
    protected[tauri] def listen(): (EventStream[this.Response], Future[() => Unit]) = {
        val (stream, callback) = EventStream.withCallback[this.Response]
        
        //TODO: handle errors - i.e., what to do when up.Read fails
        val fireEvent = (e: TauriEvent[?]) => callback(up.read[this.Response](e.payload.toString));
        
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


