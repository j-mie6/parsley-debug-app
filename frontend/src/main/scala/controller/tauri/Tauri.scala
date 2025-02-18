package controller.tauri

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

import com.raquo.laminar.api.L.*
import org.scalablytyped.runtime.StringDictionary

import typings.tauriAppsApi.coreMod.{invoke => tauriInvoke}
import typings.tauriAppsApi.eventMod.{EventCallback => EventCallbackInternal, listen => listenInternal}


/**
  * Object containing methods for communicating with the Tauri backend.
  */
object Tauri {

    /** 
      * Invoke specified backend Tauri command.
      *
      * @param cmd Name of Tauri command to invoke.
      * @return EventStream holding the result of the command call
      */
    def invoke[T](cmd: Command): EventStream[T] = invoke(cmd, Map())

    /**
      * Invoke specified backend Tauri command with arguments.
      *
      * @param cmd Name of Tauri command to invoke
      * @param args Map of argument name to argument value. Argument names should be CamelCase.
      * @return EventStream holding the result of the command call
      */
    def invoke[T](cmd: Command, args: Map[String, Any]): EventStream[T] = 
        EventStream.fromJsPromise(
            tauriInvoke(cmd.name, StringDictionary(args.toSeq: _*)),
            true
        )

    def listen(event: Event): (EventStream[event.Response], Future[() => Unit]) = event.listen()
}