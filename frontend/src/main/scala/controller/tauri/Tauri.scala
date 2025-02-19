package controller.tauri

import scala.concurrent.Future

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*
import org.scalablytyped.runtime.StringDictionary

import typings.tauriAppsApi.coreMod.{invoke => invokeInternal, InvokeArgs}
import typings.tauriAppsApi.eventMod.{EventCallback => EventCallbackInternal, listen => listenInternal}

/**
  * Object containing methods for communicating with the Tauri backend.
  */
object Tauri {
    /** 
      * Invoke specified backend Tauri command.
      *
      * @param cmd Name of Tauri command to invoke.
      * @return A future holding return value of invoked function.
      */
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture

    /**
      * Invoke specified backend Tauri command with arguments.
      *
      * @param cmd Name of Tauri command to invoke
      * @param args Map of argument name to argument value. Argument names should be CamelCase.
      * @return A future holding return value of invoked function.
      */
    def invoke[T](cmd: String, args: Map[String, Any]): Future[T] = invokeInternal(cmd, StringDictionary(args.toSeq: _*)).toFuture

    /**
      * Listen to a backend Tauri event.
      *
      * @param event Name of Tauri event to listen for.
      * @param handler function to call when event triggered.
      * @return Future holding a function to un-listen to the event.
      */
    def listen[T](event: String, handler: EventCallbackInternal[T]): Future[js.Function0[Unit]] = listenInternal(event, handler).toFuture
}