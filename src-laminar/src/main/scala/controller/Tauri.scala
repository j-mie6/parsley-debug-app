package controller

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

import scala.concurrent.Future
import org.scalablytyped.runtime.StringDictionary

import typings.tauriAppsApi.coreMod.{invoke => invokeInternal, InvokeArgs}
import typings.tauriAppsApi.eventMod.{EventCallback => EventCallbackInternal, listen => listenInternal}

/* Tauri backend interface */
object Tauri {
    /* Invoke backend command */
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture
    def invoke[T](cmd: String, args: Map[String, Any]): Future[T] = invokeInternal(cmd, StringDictionary(args.toSeq: _*)).toFuture

    /* Listen to backend event */
    def listen[T](event: String, handler: EventCallbackInternal[T]): Future[js.Function0[Unit]] = listenInternal(event, handler).toFuture
}