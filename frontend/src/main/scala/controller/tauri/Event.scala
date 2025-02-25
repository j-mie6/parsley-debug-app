package controller.tauri

import scala.util.{Try, Success, Failure}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import upickle.default as up
import typings.tauriAppsApi.eventMod.{listen => tauriListen, Event as TauriEvent}

import model.DebugTree
import controller.tauri.Event.UnlistenFn
import model.errors.MalformedJSON
import model.errors.DillException
import controller.errors.ErrorController

sealed trait Event(private val name: String) {
    
    /* Output type associated with Event */
    type Out
    given up.Reader[Out] = scala.compiletime.deferred

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
            .map(ret => (Try(up.read[Out](ret))) match {
            
                // case Success(null) => { println("It was null, fail"); Left(ErrorController.mapException(ret)) }
                case Success(suc) => { println(s"Normal success. Event: $name"); Right(suc) }
                case Failure(err) if this.isUnit && ret == "null" => { println(s"It was null, succeed. Err: $err, Ret: $ret"); Right(().asInstanceOf[Out]) }
                case Failure(err) => { println(s"Errorring while reading: $err, $ret, command was $name"); Left(ErrorController.mapException(err)) }
            })  

        (responseStream, unlisten)
    }
}


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


