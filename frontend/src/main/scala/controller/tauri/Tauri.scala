package controller.tauri

import scala.concurrent.Future

import com.raquo.laminar.api.L.*


/**
  * Object containing methods for communicating with the Tauri backend.
  */
object Tauri {

    def invoke(cmd: Command): EventStream[cmd.Response] = cmd.invoke()
    def invoke(cmd: Command, args: Map[String, Any]): EventStream[cmd.Response] = cmd.invoke(args)

    def listen(event: Event): (EventStream[event.Response], Future[() => Unit]) = event.listen()

}