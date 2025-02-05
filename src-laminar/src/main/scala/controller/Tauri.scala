package controller

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

import scala.concurrent.Future

import typings.tauriAppsApi.coreMod.{invoke => invokeInternal}
import typings.tauriAppsApi.coreMod.InvokeArgs

/**
  * DisplayTree creates the HTML element to display a DebugTree
  *
  * @param cmd Tauri command name to be called from the backend
  */
object Tauri {
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture
    def invoke[T](cmd: String, args: Map[String, Any]): Future[T] = invokeInternal(cmd, args.toJSDictionary).toFuture
}

