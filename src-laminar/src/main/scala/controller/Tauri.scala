package controller

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import typings.tauriAppsApi.coreMod.{invoke => invokeInternal}

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param cmd Tauri command name to be called from the backend
  */
object Tauri {
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture
}