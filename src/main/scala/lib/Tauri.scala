package lib

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.concurrent.Future

@js.native
@JSImport("@tauri-apps/api/core", "invoke")
private def tauriInvokeInternal[T](cmd: String): js.Promise[T] = js.native

object Tauri {
    def invoke[T](cmd: String): Future[T] = tauriInvokeInternal(cmd).toFuture
}