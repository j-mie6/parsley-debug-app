package lib

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.Future
import typings.tauriAppsApi.coreMod.{invoke => invokeInternal}

object Tauri {
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture
}