package controller

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import typings.tauriAppsApi.coreMod.{invoke => invokeInternal}
import typings.tauriAppsApi.eventMod.{EventCallback => EventCallbackInternal, listen => listenInternal}

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param cmd Tauri command name to be called from the backend
  */
object Tauri {
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture

    def listen[T](event: String, handler: EventCallbackInternal[T]): Future[js.Function0[Unit]] = listenInternal(event, handler).toFuture
}