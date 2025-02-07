package controller

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.annotation.*

import scala.concurrent.Future
import org.scalablytyped.runtime.StringDictionary

import typings.tauriAppsApi.coreMod.{invoke => invokeInternal, InvokeArgs}
import typings.tauriAppsApi.eventMod.{EventCallback => EventCallbackInternal, listen => listenInternal}
// import typings.tauriAppsApi.pathMod.

// // import { open } from '@tauri-apps/api/shell';
// // const openFacebookPage = () => {
// //   open("https://www.facebook.com/aiocean.io/")
// // }

// @js.native
// @JSImport("@tauri-apps/plugin-shell", "open")
// private def openUrlInternal[T](cmd: String): js.Promise[T] = js.native

// object Tauri {
//     def invoke[T](cmd: String): Future[T] = tauriInvokeInternal(cmd).toFuture


/* Tauri backend interface */
object Tauri {
    /** 
      * Invoke backend Tauri command
      *
      * @param cmd name of Tauri command to invoke
      * @return Future[T] holding return value of invoked function
      */
    def invoke[T](cmd: String): Future[T] = invokeInternal(cmd).toFuture

    /**
      * Invoke backend Tauri command with arguments 
      *
      * @param cmd name of Tauri command to invoke
      * @param args Map of argument name to argument value. Argument names should be CamelCase
      * @return Future[T] holding return value of invoked function
      */
    def invoke[T](cmd: String, args: Map[String, Any]): Future[T] = invokeInternal(cmd, StringDictionary(args.toSeq: _*)).toFuture

    /**
      * Listen to backend Tauri event
      *
      * @param event name of Tauri event to listen for
      * @param handler function to call when event triggered
      * @return Future holding a function to unlisten to the event
      */
    def listen[T](event: String, handler: EventCallbackInternal[T]): Future[js.Function0[Unit]] = listenInternal(event, handler).toFuture

    // def openUrl[T](cmd: String): Future[T] = openUrlInternal(cmd).toFuture
}